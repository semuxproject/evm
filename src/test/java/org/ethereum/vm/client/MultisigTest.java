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

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.TestTransactionBase;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.junit.Test;

public class MultisigTest extends TestTransactionBase {

    private long gas = 5_000_000L;
    private BigInteger gasPrice = BigInteger.TEN;

    @Test
    public void testMultisig() throws IOException {
        byte[] user1 = address(1);
        byte[] user2 = address(2);
        byte[] user3 = address(3);
        byte[] user4 = address(4);
        repository.addBalance(user1, premine);
        repository.addBalance(user2, premine);
        repository.addBalance(user3, premine);

        long nonceUser1 = 0;
        long nonceUser2 = 0;
        long nonceUser3 = 0;

        byte[] code = readContract("solidity/multisig.con");
        byte[] args = ByteArrayUtil.merge(
                DataWord.of(64).getData(),
                DataWord.of(2).getData(),
                DataWord.of(3).getData(),
                DataWord.of(user1).getData(),
                DataWord.of(user2).getData(),
                DataWord.of(user3).getData());
        byte[] contractAddress = createContract(code, args, user1, nonceUser1++, gas);

        byte[] sendRecipient = user4;
        BigInteger sendAmount = BigInteger.TEN.pow(18); // 1 SEM
        repository.addBalance(contractAddress, sendAmount); // transfer to the contract

        // submit this transaction
        byte[] txid = submitTransaction(contractAddress, sendRecipient, sendAmount, user1, nonceUser1++);

        // let user1 confirm (confirmed by user1)
        assertTrue(confirmTransaction(contractAddress, txid, user1, nonceUser1++));

        // try to commit
        executeTransaction(contractAddress, txid, user1, nonceUser1++);
        assertFalse(isConfirmed(contractAddress, txid, user1, nonceUser1++));
        assertEquals(BigInteger.ZERO, repository.getBalance(sendRecipient));

        // let user2 confirm (confirmed by user2)
        assertTrue(confirmTransaction(contractAddress, txid, user2, nonceUser2++));

        // try to commit, again
        executeTransaction(contractAddress, txid, user1, nonceUser1++);
        assertTrue(isConfirmed(contractAddress, txid, user1, nonceUser1++));
        assertEquals(sendAmount, repository.getBalance(sendRecipient));
    }

    private boolean isConfirmed(byte[] contractAddress, byte[] txid, byte[] user, long nonce) {
        byte[] method = HashUtil.keccak256("isConfirmed(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();
        assertTrue(receipt.isSuccess());

        return !DataWord.of(receipt.getReturnData()).isZero();
    }

    private boolean confirmTransaction(byte[] contractAddress, byte[] txid, byte[] user, long nonce) {
        byte[] method = HashUtil.keccak256("confirmTransaction(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();

        return receipt.isSuccess();
    }

    private boolean executeTransaction(byte[] contractAddress, byte[] txid, byte[] user, long nonce) {

        byte[] method = HashUtil.keccak256("executeTransaction(uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(txid).getData());

        Transaction transaction = new TransactionMock(false, user, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();

        return receipt.isSuccess();
    }

    private byte[] submitTransaction(byte[] contractAddress, byte[] to, BigInteger amount, byte[] user, long nonce) {
        byte[] method = HashUtil.keccak256("submitTransaction(address,uint256)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4),
                DataWord.of(to).getData(), DataWord.of(amount).getData());

        Transaction transaction = new TransactionMock(false, user, contractAddress, nonce, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();
        assertTrue(receipt.isSuccess());
        assertNotNull(receipt.getReturnData());
        return receipt.getReturnData();
    }
}
