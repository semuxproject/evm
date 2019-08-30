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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.TestTransactionBase;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.junit.Assert;
import org.junit.Test;

public class TransferTest extends TestTransactionBase {

    @Test
    public void testTransfer() throws IOException {
        long nonce = 0;

        repository.addBalance(address, premine);
        byte[] contractAddress = createContract("solidity/transfer.con", address, nonce, gas);
        nonce++;
        repository.addBalance(contractAddress, premine);

        // call method to transfer to 'caller'
        BigInteger balanceBefore = repository.getBalance(caller);

        BigInteger toSend = premine.divide(BigInteger.TEN);
        byte[] method = HashUtil.keccak256("transfer(address,uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(caller).getData(), DataWord.of(toSend).getData());

        Transaction transaction = new TransactionMock(false, address, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();

        BigInteger balanceAfter = repository.getBalance(caller);

        Assert.assertEquals(balanceBefore.add(toSend), balanceAfter);
    }

    @Test
    public void testSend() throws IOException {
        long nonce = 0;

        repository.addBalance(address, premine);
        byte[] contractAddress = createContract("solidity/transfer.con", address, nonce, gas);
        nonce++;
        repository.addBalance(contractAddress, premine);

        // call method to transfer to 'caller'
        BigInteger balanceBefore = repository.getBalance(caller);

        BigInteger toSend = premine.divide(BigInteger.TEN);
        byte[] method = HashUtil.keccak256("send(address,uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(caller).getData(), DataWord.of(toSend).getData());

        Transaction transaction = new TransactionMock(false, address, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();

        BigInteger balanceAfter = repository.getBalance(caller);

        Assert.assertEquals(balanceBefore.add(toSend), balanceAfter);
    }

    @Test
    public void testTr() throws IOException {
        long nonce = 0;

        repository.addBalance(address, premine);
        byte[] contractAddress = createContract("solidity/transfer.con", address, nonce, gas);
        nonce++;
        repository.addBalance(contractAddress, premine);

        // call method to transfer to 'caller'
        BigInteger balanceBefore = repository.getBalance(caller);

        BigInteger toSend = premine.divide(BigInteger.TEN);
        byte[] method = HashUtil.keccak256("transfer(address,uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(caller).getData(), DataWord.of(toSend).getData());

        Transaction transaction = new TransactionMock(false, address, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();

        BigInteger balanceAfter = repository.getBalance(caller);

        Assert.assertEquals(balanceBefore.add(toSend), balanceAfter);
    }
}
