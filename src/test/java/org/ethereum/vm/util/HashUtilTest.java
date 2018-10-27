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
package org.ethereum.vm.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class HashUtilTest {

    @Test
    public void testKeccak256() {
        byte[] msg = "testing".getBytes();
        byte[] keccak256 = HashUtil.keccak256(msg);

        String expected = "5f16f4c7f149ac4f9510d9cf8cf384038ad348b3bcdc01915f95de12df9d1b02";
        assertEquals(expected, HexUtil.toHexString(keccak256));
    }

    @Test
    public void testCalcNewAddress() {
        byte[] address = new byte[20];
        long nonce = 0;
        assertEquals(20, HashUtil.calcNewAddress(address, nonce).length);
    }

    @Test
    public void calcSaltAddressTest() {
        byte[] from = Hex.decode("0123456789012345678901234567890123456789");
        byte[] salt = Hex.decode("0000000000000000000000000000000000000000000000000000000000000314");
        // contract Demo{}
        byte[] code = Hex.decode(
                "6080604052348015600f57600080fd5b50603580601d6000396000f3006080604052600080fd00a165627a7a72305820a63607f79a5e21cdaf424583b9686f2aa44059d70183eb9846ccfa086405716e0029");
        assertArrayEquals(Hex.decode("d26e42c8a0511c19757f783402231cf82b2bdf59"),
                HashUtil.calcSaltAddress(from, code, salt));
    }
}
