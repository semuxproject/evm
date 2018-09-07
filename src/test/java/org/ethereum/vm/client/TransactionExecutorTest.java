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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.ethereum.vm.TestBase;
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
    public void additionalSetup() {
        transaction = new TransactionMock(false, caller, address, nonce, value, data, BigInteger.valueOf(gas),
                gasPrice);
        block = new BlockMock(BigInteger.valueOf(gasLimit), prevHash, coinbase, timestamp, number);
        repository.addBalance(caller, premine);
    }

    @Test
    public void testBasicTx() {
        // transfer 1 ETH
        transaction = spy(transaction);
        when(transaction.getValue()).thenReturn(Unit.ETH);
        when(transaction.getGas()).thenReturn(BigInteger.valueOf(21_000L + 1000L));

        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();
        System.out.println(receipt);

        assertFalse(receipt.isFailed());
        assertEquals(21_000L, receipt.getGasUsed());
        assertArrayEquals(new byte[0], receipt.getReturnData());
        assertTrue(receipt.getLogs().isEmpty());

        BigInteger balance1 = repository.getBalance(caller);
        BigInteger balance2 = repository.getBalance(address);
        assertEquals(premine.subtract(Unit.ETH).subtract(BigInteger.valueOf(21_000L)), balance1);
        assertEquals(Unit.ETH, balance2);
    }
}
