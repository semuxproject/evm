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
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MultisigTest extends TestTransactionBase {

    protected final byte[] user2 = address(3);
    protected final byte[] user3 = address(4);
    private long gas = 4_000_000L;

    @Test
    @Ignore("Cannot yet figure out constructor args")
    public void testMultisig() throws IOException {

        byte[] data = readContract("solidity/multisig.con");

        // constructor constructor(address[] _owners, uint _required)
        byte[] constructor = HashUtil.keccak256("(address[],unit256)".getBytes(StandardCharsets.UTF_8));
        byte[] constructorData = ByteArrayUtil.merge(Arrays.copyOf(constructor, 4),
                DataWord.of(3).getData(), // number of addresses
                DataWord.of(caller).getData(),
                DataWord.of(user2).getData(),
                DataWord.of(user3).getData(),
                DataWord.of(2).getData() // how many are required to use (2 of 3)
        );

        byte[] fullContract = ByteArrayUtil.merge(data, constructorData);
        byte[] contractAddress = HashUtil.calcNewAddress(caller, nonce);

        Transaction transaction = new TransactionMock(true, caller, address, nonce, value, fullContract, gas, gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        Assert.assertTrue(receipt.isSuccess());

    }
}
