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
package org.ethereum.vm.chainspec;

import java.math.BigInteger;

import org.ethereum.vm.client.Repository;

public interface PrecompiledContractContext {

    /**
     * Returns the repository track.
     *
     * @return the current repository track.
     */
    Repository getTrack();

    /**
     * Returns the caller's address.
     *
     * @return an address
     */
    byte[] getCaller();

    /**
     * Returns the value being transferred to this contract.
     *
     * NOTE: the transfer has been conducted.
     *
     * @return a value in {@link org.ethereum.vm.client.Unit#WEI}.
     */
    BigInteger getValue();

    /**
     * Returns the data passed to this contract.
     *
     * @return a byte array
     */
    byte[] getData();
}
