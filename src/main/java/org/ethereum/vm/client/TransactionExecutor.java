/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.client;

import static org.ethereum.vm.util.BigIntegerUtil.isCovers;
import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;

import java.math.BigInteger;
import java.util.ArrayList;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.chainspec.Spec;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.ethereum.vm.util.ByteArrayWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);

    private final Transaction tx;
    private final Block block;
    private long basicTxCost;

    private final Repository repo;
    private final BlockStore blockStore;

    private final Spec spec;
    private final ProgramInvokeFactory invokeFactory;
    private final long gasUsedInTheBlock;

    private TransactionReceipt receipt;

    public TransactionExecutor(Transaction tx, Block block, Repository repo, BlockStore blockStore) {
        this(tx, block, repo, blockStore, Spec.DEFAULT, new ProgramInvokeFactoryImpl(), 0);
    }

    public TransactionExecutor(Transaction tx, Block block, Repository repo, BlockStore blockStore,
            Spec spec, ProgramInvokeFactory invokeFactory, long gasUsedInTheBlock) {
        this.tx = tx;
        this.block = block;
        this.basicTxCost = spec.getTransactionCost(tx);

        this.repo = repo;
        this.blockStore = blockStore;

        this.spec = spec;
        this.invokeFactory = invokeFactory;
        this.gasUsedInTheBlock = gasUsedInTheBlock;
    }

    /**
     * Do basic validation, e.g. nonce, balance and gas check.
     *
     * @return true if the transaction is ready to execute; otherwise false.
     */
    protected boolean prepare() {
        BigInteger txGas = BigInteger.valueOf(tx.getGas());
        BigInteger blockGasLimit = BigInteger.valueOf(block.getGasLimit());

        if (txGas.add(BigInteger.valueOf(gasUsedInTheBlock)).compareTo(blockGasLimit) > 0) {
            logger.warn("Too much gas used in this block");
            return false;
        }

        if (txGas.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {
            logger.warn("Not enough gas to cover basic transaction cost: required = {}, actual = {}", basicTxCost,
                    txGas);
            return false;
        }

        long reqNonce = repo.getNonce(tx.getFrom());
        long txNonce = tx.getNonce();
        if (reqNonce != txNonce) {
            logger.warn("Invalid nonce: required = {}, actual = {}", reqNonce, txNonce);
            return false;
        }

        BigInteger txGasCost = tx.getGasPrice().multiply(txGas);
        BigInteger totalCost = tx.getValue().add(txGasCost);
        BigInteger senderBalance = repo.getBalance(tx.getFrom());
        if (!isCovers(senderBalance, totalCost)) {
            logger.warn("Not enough balance: required = {}, actual = {}", totalCost, senderBalance);
            return false;
        }

        return true;
    }

    /**
     * Executes the transaction.
     */
    protected ProgramResult execute() {
        // [PRE-INVOKE] debit the gas from the sender's account
        repo.addBalance(tx.getFrom(), tx.getGasPrice().multiply(BigInteger.valueOf(tx.getGas())).negate());

        // phantom invoke
        byte[] ops = EMPTY_BYTE_ARRAY;
        ProgramInvoke invoke = invokeFactory.createProgramInvoke(tx, block, repo, blockStore);
        Program program = new Program(ops, invoke, spec);

        // [1] spend basic transaction cost
        program.spendGas(basicTxCost, "Basic transaction cost");

        // [2] make a CALL/CREATE invocation
        ProgramResult invokeResult;
        byte[] txData = tx.getData();
        program.memorySave(0, txData); // write the tx data into memory
        if (tx.isCreate()) {
            long gas = program.getGasLeft();

            // nonce and gas spending are within the createContract method

            invokeResult = program.createContract(DataWord.of(tx.getValue()), DataWord.ZERO,
                    DataWord.of(txData.length), gas);
        } else {
            OpCode type = OpCode.CALL;
            long gas = program.getGasLeft();
            DataWord codeAddress = DataWord.of(tx.getTo());
            DataWord value = DataWord.of(tx.getValue());
            DataWord inDataOffs = DataWord.ZERO;
            DataWord inDataSize = DataWord.of(txData.length);
            DataWord outDataOffs = DataWord.of(txData.length);
            DataWord outDataSize = DataWord.ZERO;

            // increase the nonce of sender
            repo.increaseNonce(tx.getFrom());
            // spend the call cost
            program.spendGas(gas, "transaction call");

            invokeResult = program.callContract(type, gas, codeAddress, value, inDataOffs, inDataSize, outDataOffs,
                    outDataSize);
        }

        // [3] post-invocation processing
        if (invokeResult.getException() == null && !invokeResult.isRevert()) {
            // commit deleted accounts
            for (ByteArrayWrapper address : invokeResult.getDeleteAccounts()) {
                repo.delete(address.getData());
            }

            // handle future refund
            long gasUsed = program.getGasUsed();
            long suicideRefund = invokeResult.getDeleteAccounts().size() * spec.getFeeSchedule().getSUICIDE_REFUND();
            long qualifiedRefund = Math.min(invokeResult.getFutureRefund() + suicideRefund, gasUsed / 2);
            program.refundGas(qualifiedRefund, "Future refund");
            program.resetFutureRefund();
        }

        // [4] fix the program's result
        ProgramResult result = program.getResult();
        result.setReturnData(invokeResult.getReturnData());
        result.setException(invokeResult.getException());
        result.setRevert(invokeResult.isRevert());
        result.setInternalTransactions(new ArrayList<>(invokeResult.getInternalTransactions()));
        // others have been merged after the invocation

        // [POST-INVOKE] credit the sender for remaining gas
        repo.addBalance(tx.getFrom(), tx.getGasPrice().multiply(BigInteger.valueOf(program.getGasLeft())));

        return result;
    }

    /**
     * Execute the transaction and returns a receipt.
     *
     * @return a transaction receipt, or NULL if the transaction is rejected
     */
    public TransactionReceipt run() {
        if (receipt != null) {
            return receipt;
        }

        // prepare
        if (!prepare()) {
            return null;
        }

        // execute
        ProgramResult result = execute();

        receipt = new TransactionReceipt(tx,
                result.getException() == null && !result.isRevert(),
                result.getGasUsed(),
                result.getReturnData(),
                result.getLogs(),
                new ArrayList<>(result.getDeleteAccounts()),
                result.getInternalTransactions());
        return receipt;
    }
}
