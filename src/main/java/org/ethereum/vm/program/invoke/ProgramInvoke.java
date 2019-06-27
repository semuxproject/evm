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
package org.ethereum.vm.program.invoke;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.Repository;

/**
 * Represents a program invoke.
 */
public interface ProgramInvoke {

    // ===========================
    // Transaction context
    // ===========================

    /**
     * Returns the address of currently executing account.
     *
     * @return an address, right-aligned
     */
    DataWord getOwnerAddress();

    /**
     * Returns the execution origination address.
     *
     * @return an address, right-aligned
     */
    DataWord getOriginAddress();

    /**
     * Returns the caller address.
     *
     * @return an address, right-aligned
     */
    DataWord getCallerAddress();

    /**
     * Returns the gas limit for the invocation.
     *
     * @return gas limit
     */
    long getGas();

    /**
     * Returns the gas price.
     *
     * @return the gas price in {@link org.ethereum.vm.client.Unit#WEI} per gas
     */
    DataWord getGasPrice();

    /**
     * Returns the deposited value by the instruction/transaction responsible for
     * this execution.
     *
     * @return the call value in {@link org.ethereum.vm.client.Unit#WEI}
     */
    DataWord getValue();

    /**
     * Returns the size of call data.
     *
     * @return
     */
    DataWord getDataSize();

    /**
     * Returns the data at the given offset.
     *
     * @param offset
     *            an offset
     * @return a word starting from the offset; zeros are padded if out of range.
     */
    DataWord getDataValue(DataWord offset);

    /**
     * Returns the given number of bytes, starting from an offset.
     *
     * @param offset
     *            the starting offset
     * @param length
     *            the number of bytes to copy
     * @return a byte array copied from the call data; zeros are padded if out of
     *         range.
     */
    byte[] getDataCopy(DataWord offset, DataWord length);

    // ===========================
    // Block context
    // ===========================

    /**
     * Returns the hash of the previous block.
     *
     * @return a block hash.
     */
    DataWord getPrevHash();

    /**
     * Returns the miner address of this block.
     *
     * @return an address, right-aligned.
     */
    DataWord getCoinbase();

    /**
     * Returns the timestamp of this block.
     *
     * @return the timestamp.
     */
    DataWord getTimestamp();

    /**
     * Returns the number of this block.
     *
     * @return the block number
     */
    DataWord getNumber();

    /**
     * Returns the difficulty of this block.
     *
     * @return the block difficulty
     */
    DataWord getDifficulty();

    /**
     * Returns the gas limit of this block.
     *
     * @return the block gas limit
     */
    DataWord getGaslimit();

    // ===========================
    // Database context
    // ===========================

    /**
     * Returns the repository interface.
     *
     * @return repository implementation
     */
    Repository getRepository();

    /**
     * Returns the original repository.
     */
    Repository getOriginalRepository();

    /**
     * Returns the block storage interface.
     *
     * @return block store implementation
     */
    BlockStore getBlockStore();

    // ===========================
    // Miscellaneous
    // ===========================

    /**
     * Returns the current call depth. It should return 0 for a normal transaction.
     *
     * @return the call depth.
     */
    int getCallDepth();

    /**
     * Returns whether this invocation is a static call.
     *
     * @return true if it's a static call; otherwise false.
     */
    boolean isStaticCall();
}
