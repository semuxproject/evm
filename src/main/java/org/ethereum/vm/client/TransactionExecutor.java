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
import static org.ethereum.vm.util.BigIntegerUtil.toBI;
import static org.ethereum.vm.util.BigIntegerUtil.transfer;
import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.vm.util.ByteArrayUtil.getLength;
import static org.ethereum.vm.util.ByteArrayUtil.isEmpty;
import static org.ethereum.vm.util.HexUtil.toHexString;

import java.math.BigInteger;
import java.util.ArrayList;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.VM;
import org.ethereum.vm.chainspec.PrecompiledContract;
import org.ethereum.vm.chainspec.PrecompiledContractContext;
import org.ethereum.vm.chainspec.Spec;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.exception.ExceptionFactory;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.ethereum.vm.util.ByteArrayWrapper;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);

    private final Transaction tx;
    private final Block block;
    private long basicTxCost;

    private final Repository repo;
    private final Repository track;
    private final BlockStore blockStore;

    private final Spec spec;
    private final ProgramInvokeFactory invokeFactory;
    private final long gasUsedInTheBlock;

    private VM vm;
    private Program program;

    private ProgramResult result = new ProgramResult();
    private BigInteger gasLeft;

    public TransactionExecutor(Transaction tx, Block block, Repository repo, BlockStore blockStore) {
        this(tx, block, repo, blockStore, Spec.DEFAULT, new ProgramInvokeFactoryImpl(), 0);
    }

    public TransactionExecutor(Transaction tx, Block block, Repository repo, BlockStore blockStore,
            Spec spec, ProgramInvokeFactory invokeFactory, long gasUsedInTheBlock) {
        this.tx = tx;
        this.block = block;
        this.basicTxCost = spec.getTransactionCost(tx);

        this.repo = repo;
        this.track = repo.startTracking();
        this.blockStore = blockStore;

        this.spec = spec;
        this.invokeFactory = invokeFactory;
        this.gasUsedInTheBlock = gasUsedInTheBlock;

        this.gasLeft = BigInteger.valueOf(tx.getGas());
    }

    /**
     * Do basic validation, e.g. nonce, balance and gas check, and prepare this
     * executor.
     *
     * @return true if the transaction is ready to prepare; otherwise false.
     */
    protected boolean init() {
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
    protected void prepare() {
        // increase nonce
        repo.increaseNonce(tx.getFrom());

        // charge gas cost
        BigInteger txGasLimit = BigInteger.valueOf(tx.getGas());
        BigInteger txGasCost = tx.getGasPrice().multiply(txGasLimit);
        repo.addBalance(tx.getFrom(), txGasCost.negate());

        if (tx.isCreate()) {
            create();
        } else {
            call();
        }
    }

    protected void call() {
        byte[] targetAddress = tx.getTo();
        PrecompiledContract precompiledContract = spec.getPrecompiledContracts()
                .getContractForAddress(DataWord.of(targetAddress));

        // transfer value
        BigInteger endowment = tx.getValue();
        transfer(track, tx.getFrom(), targetAddress, endowment);

        if (precompiledContract != null) {
            long requiredGas = precompiledContract.getGasForData(tx.getData());

            BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));
            if (gasLeft.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
                logger.warn("Out of Gas calling precompiled contract: required {}, gasLeft = {}", spendingGas, gasLeft);
                gasLeft = BigInteger.ZERO;
            } else {
                gasLeft = gasLeft.subtract(spendingGas);
                Pair<Boolean, byte[]> out = precompiledContract.execute(new PrecompiledContractContext() {
                    @Override
                    public Repository getTrack() {
                        return track;
                    }

                    @Override
                    public byte[] getCaller() {
                        return tx.getFrom();
                    }

                    @Override
                    public BigInteger getValue() {
                        return endowment;
                    }

                    @Override
                    public byte[] getData() {
                        return tx.getData();
                    }
                });

                if (!out.getLeft()) {
                    logger.warn("Error executing precompiled contract 0x{}", toHexString(targetAddress));
                    gasLeft = BigInteger.ZERO;
                }
            }
        } else {
            byte[] code = track.getCode(targetAddress);
            if (isEmpty(code)) {
                gasLeft = gasLeft.subtract(BigInteger.valueOf(basicTxCost));
                result.spendGas(basicTxCost);
            } else {
                ProgramInvoke programInvoke = invokeFactory.createProgramInvoke(tx, block, track, blockStore);

                this.vm = new VM(spec);
                this.program = new Program(code, programInvoke, tx, spec);
            }
        }
    }

    protected void create() {
        byte[] newContractAddress = HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce());

        if (track.exists(newContractAddress)) {
            logger.warn("Contract already exists: address = 0x{}", toHexString(newContractAddress));
            gasLeft = BigInteger.ZERO;
            return;
        }

        // In case of hashing collisions
        BigInteger oldBalance = repo.getBalance(newContractAddress);
        track.addBalance(newContractAddress, oldBalance);
        track.increaseNonce(newContractAddress);

        // transfer value
        BigInteger endowment = tx.getValue();
        transfer(track, tx.getFrom(), newContractAddress, endowment);

        if (isEmpty(tx.getData())) {
            gasLeft = gasLeft.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            ProgramInvoke programInvoke = invokeFactory.createProgramInvoke(tx, block, track, blockStore);

            this.vm = new VM(spec);
            this.program = new Program(tx.getData(), programInvoke, tx, spec);
        }
    }

    protected void execute() {
        try {
            if (vm == null) { // no vm involved
                track.commit();
            } else {
                // charge basic cost of the transaction
                program.spendGas(basicTxCost, "basic transaction cost");

                vm.play(program);

                // overwrites result
                result = program.getResult();
                gasLeft = BigInteger.valueOf(tx.getGas()).subtract(toBI(result.getGasUsed()));

                if (tx.isCreate() && !result.isRevert()) {
                    int returnDataGasValue = getLength(result.getReturnData())
                            * spec.getFeeSchedule().getCREATE_DATA();

                    if (gasLeft.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                        // Not enough gas to return contract code
                        if (!spec.createEmptyContractOnOOG()) {
                            program.setRuntimeFailure(
                                    ExceptionFactory.notEnoughSpendingGas("No gas to return just created contract",
                                            returnDataGasValue, program));
                        }
                        result.setReturnData(EMPTY_BYTE_ARRAY);
                    } else if (getLength(result.getReturnData()) > spec.maxContractSize()) {
                        // Contract size too large
                        program.setRuntimeFailure(ExceptionFactory
                                .notEnoughSpendingGas("Contract size too large: " + getLength(result.getReturnData()),
                                        returnDataGasValue, program));
                        result.setReturnData(EMPTY_BYTE_ARRAY);
                    } else {
                        // Contract successfully created
                        gasLeft = gasLeft.subtract(BigInteger.valueOf(returnDataGasValue));
                        track.saveCode(HashUtil.calcNewAddress(tx.getFrom(), tx.getNonce()), result.getReturnData());
                    }
                }

                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogs().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        rollback();
                        logger.warn("Exception occurred", result.getException()); // defined exception
                    } else {
                        logger.warn("REVERT opcode executed");
                    }
                } else {
                    track.commit();
                }
            }
        } catch (Exception e) {
            rollback();
            logger.error("Unexpected exception", e); // unexpected, double check required
        }
    }

    /**
     * Finalize all the changes to repository and builds a summary.
     *
     * @return a transaction summary, or NULL if the transaction fails the at
     *         {@link #init()}.
     */
    public TransactionReceipt run() {
        if (!init()) {
            return null;
        } else {
            prepare();
            execute();
        }

        // accumulate refunds for suicides
        result.addFutureRefund(result.getDeleteAccounts().size() * spec.getFeeSchedule().getSUICIDE_REFUND());
        long gasRefund = Math.min(result.getFutureRefund(), getGasUsed() / 2);
        gasLeft = gasLeft.add(BigInteger.valueOf(gasRefund));

        // commit deleted accounts
        for (ByteArrayWrapper address : result.getDeleteAccounts()) {
            repo.delete(address.getData());
        }

        // refund
        BigInteger totalRefund = gasLeft.multiply(tx.getGasPrice());
        repo.addBalance(tx.getFrom(), gasLeft.multiply(tx.getGasPrice()));
        logger.debug("Pay total refund to sender: amount = {}", totalRefund);

        return new TransactionReceipt(tx,
                result.getException() == null && !result.isRevert(),
                getGasUsed(),
                result.getReturnData(),
                result.getLogs(), new ArrayList<>(result.getDeleteAccounts()), result.getInternalTransactions());
    }

    private void rollback() {
        track.rollback();

        gasLeft = BigInteger.ZERO;
    }

    private long getGasUsed() {
        return tx.getGas() - gasLeft.longValue();
    }
}
