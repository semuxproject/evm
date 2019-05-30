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
package org.ethereum.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.ethereum.vm.client.Transaction;
import org.ethereum.vm.client.TransactionExecutor;
import org.ethereum.vm.client.TransactionReceipt;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import org.junit.Test;

public class PrecompiledContractsByzantiumTest extends TestTransactionBase {

    @Test
    public void testECRecover() {
        // contract Test {
        // function verify(bytes32 hash, uint8 v, bytes32 r, bytes32 s) constant
        // returns(address) {
        // bytes memory prefix = "\x19Ethereum Signed Message:\n32";
        // bytes32 prefixedHash = keccak256(prefix, hash);
        // return ecrecover(prefixedHash, v, r, s);
        // }
        // }
        String code = "608060405234801561001057600080fd5b5061024c806100206000396000f300608060405260043610610041576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063f1835db714610046575b600080fd5b34801561005257600080fd5b5061009e6004803603810190808035600019169060200190929190803560ff169060200190929190803560001916906020019092919080356000191690602001909291905050506100e0565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6000606060006040805190810160405280601c81526020017f19457468657265756d205369676e6564204d6573736167653a0a333200000000815250915081876040518083805190602001908083835b6020831015156101555780518252602082019150602081019050602083039250610130565b6001836020036101000a03801982511681845116808217855250505050505090500182600019166000191681526020019250505060405180910390209050600181878787604051600081526020016040526040518085600019166000191681526020018460ff1660ff1681526020018360001916600019168152602001826000191660001916815260200194505050505060206040516020810390808403906000865af115801561020a573d6000803e3d6000fd5b50505060206040510351925050509493505050505600a165627a7a72305820c28038a95a2d8c5fee2fb4c1ba7b20c6ee5405e3528f5d6883bae1108a17987a0029";
        byte[] contractAddress = deploy(code, DataWord.ONE.getData());

        byte[] method = Arrays
                .copyOf(HashUtil.keccak256("verify(bytes32,uint8,bytes32,bytes32)".getBytes(StandardCharsets.UTF_8)),
                        4);
        byte[] hash = HashUtil.keccak256("hello".getBytes(StandardCharsets.UTF_8));
        System.out.println(HexUtil.toHexString(hash));
        byte[] v = DataWord.of(28).getData();
        byte[] r = HexUtil.fromHexString("9242685bf161793cc25603c231bc2f568eb630ea16aa137d2664ac8038825608");
        byte[] s = HexUtil.fromHexString("4f8ae3bd7535248d0bd448298cc2e2071e56992d0774dc340c368ae950852ada");
        byte[] data = ByteArrayUtil.merge(method, hash, v, r, s);

        Transaction tx = spy(transaction);
        when(tx.getTo()).thenReturn(contractAddress);
        when(tx.getData()).thenReturn(data);

        TransactionExecutor executor = new TransactionExecutor(tx, block, repository, blockStore, true);
        TransactionReceipt receipt = executor.run();

        assertTrue(receipt.isSuccess());
        assertEquals(DataWord.of("7156526fbd7a3c72969b54f64e42c10fbb768c8a"), DataWord.of(receipt.getReturnData()));
    }
}
