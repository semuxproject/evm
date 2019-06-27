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
import org.ethereum.vm.util.HexUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Erc20Test extends TestTransactionBase {

    private byte[] erc20owner = HexUtil.fromHexString("23a6049381fd2cfb0661d9de206613b83d53d7df");
    private long gas = 2_000_000L;

    @Before
    public void setup() {
        super.setup();
        repository.addBalance(erc20owner, premine);
    }

    @Test
    public void testErc20Token() throws IOException {
        byte[] contractAddress = createContract("solidity/erc20.con", erc20owner, nonce, gas);

        // check balance
        byte[] method = HashUtil.keccak256("balanceOf(address)".getBytes(StandardCharsets.UTF_8));
        byte[] methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(erc20owner).getData());

        transaction = new TransactionMock(false, erc20owner, contractAddress, nonce + 1, value, methodData, gas,
                gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();
        Assert.assertTrue(receipt.isSuccess());
        BigInteger balance = DataWord.of(receipt.getReturnData()).value();
        Assert.assertEquals(new BigInteger("100000000000000000000000000"), balance);

        // transfer
        method = HashUtil.keccak256("transfer(address,uint256)".getBytes(StandardCharsets.UTF_8));
        methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(address).getData(),
                DataWord.of(BigInteger.TEN).getData());
        transaction = new TransactionMock(false, erc20owner, contractAddress, nonce + 2, value, methodData, gas,
                gasPrice);
        executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        receipt = executor.run();
        Assert.assertTrue(receipt.isSuccess());

        // check balance of target
        method = HashUtil.keccak256("balanceOf(address)".getBytes(StandardCharsets.UTF_8));
        methodData = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(address).getData());

        transaction = new TransactionMock(false, erc20owner, contractAddress, nonce + 3, value, methodData, gas,
                gasPrice);
        executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        receipt = executor.run();
        Assert.assertTrue(receipt.isSuccess());
        balance = DataWord.of(receipt.getReturnData()).value();
        Assert.assertEquals(new BigInteger("10"), balance);
    }
}
