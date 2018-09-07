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

import org.bouncycastle.util.encoders.Hex;

public class HexUtil {

    public static byte[] fromHexString(String hex) {
        return Hex.decode(hex);
    }

    public static String toHexString(byte[] data) {
        return Hex.toHexString(data);
    }

    public static String toHexString(byte b) {
        return toHexString(new byte[] { b });
    }
}
