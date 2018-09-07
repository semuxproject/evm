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

import java.math.BigInteger;

import org.ethereum.vm.TestBase;
import org.junit.Test;

public class TransactionExecutorTest extends TestBase {

    @Test
    public void testBasicTx() {
        Transaction tx = new TransactionMock(
                false,
                address(1),
                address(2),
                0,
                BigInteger.ONE.multiply(Unit.ETH),
                new byte[0],
                BigInteger.valueOf(22_000),
                BigInteger.ONE.multiply(Unit.GWEI));
        Block block = new BlockMock(
                BigInteger.valueOf(1_000_000L),
                new byte[32],
                address(3),
                System.currentTimeMillis(),
                1);
        BigInteger premine = BigInteger.TEN.multiply(Unit.ETH);
        BigInteger gasUsage = BigInteger.valueOf(21_000L);
        repository.addBalance(address(1), premine);

        TransactionExecutor executor = new TransactionExecutor(tx, block, repository, blockStore, false);
        TransactionSummary summary = executor.run();
        System.out.println(summary);

        assertFalse(summary.isFailed());
        assertEquals(gasUsage, summary.getGasUsed());
        assertArrayEquals(new byte[0], summary.getReturnData());
        assertTrue(summary.getLogs().isEmpty());

        BigInteger balance1 = repository.getBalance(address(1));
        BigInteger balance2 = repository.getBalance(address(2));
        BigInteger balance3 = repository.getBalance(address(3));
        assertEquals(premine.subtract(tx.getValue()).subtract(gasUsage.multiply(tx.getGasPrice())), balance1);
        assertEquals(tx.getValue(), balance2);
        // TODO: How is miner get paid
        // assertEquals(BigInteger.valueOf(21_000L).multiply(tx.getGasPrice()),
        // balance3);
    }
}
