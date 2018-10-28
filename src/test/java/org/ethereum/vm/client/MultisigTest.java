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

import org.ethereum.vm.DataWord;
import org.ethereum.vm.TestTransactionBase;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MultisigTest extends TestTransactionBase {

    protected final byte[] user2 = address(3);
    protected final byte[] user3 = address(4);
    protected final byte[] user4 = address(5);
    private long gas = 4_000_000L;

    @Test
    public void testMultisig() throws IOException {

        repository.addBalance(user2, premine);
        repository.addBalance(user3, premine);

        byte[] contractAddress = createContract("solidity/multisig.con", user2, nonce, gas);

        // make sure the contract is funded
        repository.addBalance(contractAddress, premine);

        nonce++;

        BigInteger toSend = premine.divide(BigInteger.TEN);
        byte[] txid = createTransaction(contractAddress, user3, user4, toSend, 0);

        // try to commit
        executeTransaction(contractAddress, txid, nonce);
        // check balance of user 4
        BigInteger balance = repository.getBalance(user4);
        boolean isConfirmed = isConfirmed(contractAddress, txid);
        Assert.assertFalse(isConfirmed);

        // let another user confirm
        confirmTransaction(contractAddress, user2, txid);
        isConfirmed = isConfirmed(contractAddress, txid);
        // Assert.assertTrue(isConfirmed);
        boolean success = executeTransaction(contractAddress, txid, 3);

        BigInteger balanceFinal = repository.getBalance(user4);

        Assert.assertEquals(toSend, balanceFinal);
        int i = 0;
    }

    private boolean isConfirmed(byte[] contractAddress, byte[] txid) {
        byte[] method = HashUtil.keccak256("isConfirmed(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user2, contractAddress, 2, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, true);
        TransactionReceipt receipt = executor.run();
        Assert.assertTrue(receipt.isSuccess());

        return !DataWord.of(receipt.getReturnData()).isZero();
    }

    private boolean confirmTransaction(byte[] contractAddress, byte[] user, byte[] txid) {
        byte[] method = HashUtil.keccak256("confirmTransaction(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user, contractAddress, 2, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        return receipt.isSuccess();
    }

    private boolean executeTransaction(byte[] contractAddress, byte[] txid, long nonce) {

        byte[] method = HashUtil.keccak256("executeTransaction(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user2, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        return receipt.isSuccess();
    }

    private void testFoo(byte[] contractAddress) {

        byte[] method = HashUtil.keccak256("foo()".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = Arrays.copyOf(method, 4);

        Transaction transaction = new TransactionMock(false, user2, contractAddress, 1, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        Assert.assertTrue(receipt.isSuccess());
    }

    private byte[] createTransaction(byte[] contractAddress, byte[] from, byte[] to, BigInteger amount, long nonce) {
        byte[] method = HashUtil.keccak256("submitTransaction(address,uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(to).getData(), DataWord.of(amount).getData());

        Transaction transaction = new TransactionMock(false, from, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();
        Assert.assertTrue(receipt.isSuccess());
        Assert.assertNotNull(receipt.getReturnData());
        return receipt.getReturnData();
    }
}
