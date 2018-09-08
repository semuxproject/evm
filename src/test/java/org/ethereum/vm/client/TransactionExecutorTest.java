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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.FeeSchedule;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.TestBase;
import org.ethereum.vm.config.Config;
import org.ethereum.vm.program.InternalTransaction;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.BytecodeCompiler;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import org.junit.Before;
import org.junit.Test;

public class TransactionExecutorTest extends TestBase {

    protected final BigInteger premine = BigInteger.valueOf(100L).multiply(Unit.ETH);
    protected final boolean isCreate = false;
    protected final long nonce = 0;

    // by default, it's a CALL transaction with 1 million gas and empty payload
    protected Transaction transaction;
    protected Block block;

    @Before
    public void setup() {
        super.setup();
        transaction = new TransactionMock(isCreate, caller, address, nonce, value, data, gas, gasPrice);
        block = new BlockMock(BigInteger.valueOf(gasLimit), prevHash, coinbase, timestamp, number);
        repository.addBalance(caller, premine);
    }

    @Test
    public void testBasicTx() {
        // transfer 1 ETH
        transaction = spy(transaction);
        when(transaction.getValue()).thenReturn(Unit.ETH);
        when(transaction.getGas()).thenReturn(21_000L + 1000L);

        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();
        System.out.println(receipt);

        assertTrue(receipt.isSuccess());
        assertEquals(21_000L, receipt.getGasUsed());
        assertArrayEquals(new byte[0], receipt.getReturnData());
        assertTrue(receipt.getLogs().isEmpty());

        BigInteger balance1 = repository.getBalance(caller);
        BigInteger balance2 = repository.getBalance(address);
        assertEquals(premine.subtract(Unit.ETH).subtract(BigInteger.valueOf(21_000L)), balance1);
        assertEquals(Unit.ETH, balance2);
    }

    @Test
    public void testRecursiveCall() {
        // contract Test {
        // function f(uint n) {
        // if (n > 0) {
        // this.call(bytes4(sha3("f(uint256)")), n - 1);
        // }
        // }
        // }
        byte[] code = HexUtil.fromHexString(
                "608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063b3de648b14610046575b600080fd5b34801561005257600080fd5b5061007160048036038101908080359060200190929190505050610073565b005b6000811115610139573073ffffffffffffffffffffffffffffffffffffffff1660405180807f662875696e743235362900000000000000000000000000000000000000000000815250600a01905060405180910390207c01000000000000000000000000000000000000000000000000000000009004600183036040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808281526020019150506000604051808303816000875af192505050505b5056");
        repository.saveCode(address, code);

        byte[] method = HashUtil.keccak256("f(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), new DataWord(1000).getData());
        transaction = spy(transaction);
        when(transaction.getData()).thenReturn(data);

        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        assertTrue(receipt.isSuccess());
        assertTrue(receipt.getInternalTransactions().size() > 1);
        assertTrue(receipt.getInternalTransactions().size() < 1000); // due the 63/64 gas rule
        assertEquals(0, receipt.getReturnData().length);
        assertTrue(receipt.getGasUsed() < gas);

        List<InternalTransaction> txs = receipt.getInternalTransactions();
        for (int i = 0; i < txs.size(); i++) {
            InternalTransaction tx = txs.get(i);
            assertEquals(i, tx.getDepth());
            assertEquals(0, tx.getIndex());
            assertEquals(OpCode.CALL, tx.getType());
            assertEquals(i >= txs.size() - 2, tx.isRejected());
        }
    }

    @Test
    public void testCallWithMaxGas() {
        String asm = "PUSH1 0x88" // out size
                + " PUSH1 0x00" // out offset
                + " PUSH1 0x00" // in size
                + " PUSH1 0x00" // in offset
                + " PUSH1 0x01" // value
                + " PUSH20 0x" + Hex.toHexString(address(128)) // address
                + " PUSH32 0x" + new DataWord(DataWord.MAX_VALUE) // gas
                + " CALL";
        byte[] code = BytecodeCompiler.compile(asm);
        repository.saveCode(address, code);
        repository.addBalance(address, BigInteger.ONE.multiply(Unit.ETH));

        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        assertTrue(receipt.isSuccess());
        assertEquals(1, receipt.getInternalTransactions().size());

        Config config = Config.DEFAULT;
        FeeSchedule fs = config.getFeeSchedule();
        int memWords = (0x88 + 31) / 32;
        long availableGas = gas - config.getTransactionCost(transaction) // basic cost
                - OpCode.Tier.VeryLowTier.asInt() * 7 // 7 push ops
                - fs.getCALL() // call
                - fs.getVT_CALL() // extra: value transfer
                - (fs.getMEMORY() * memWords + memWords * memWords / 512 - 0) // memory expansion
        ;
        assertEquals(availableGas - availableGas / 64 + fs.getSTIPEND_CALL(),
                receipt.getInternalTransactions().get(0).getGas());
    }
}
