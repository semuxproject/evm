/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.client;

import java.math.BigInteger;

public class BlockMock implements Block {

    private BigInteger gasLimit;
    private byte[] parentHash;
    private byte[] coinbase;
    private long timestamp;
    private long number;

    public BlockMock(BigInteger gasLimit, byte[] parentHash, byte[] coinbase, long timestamp, long number) {
        this.gasLimit = gasLimit;
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
    }

    @Override
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    @Override
    public byte[] getParentHash() {
        return parentHash;
    }

    @Override
    public byte[] getCoinbase() {
        return coinbase;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getNumber() {
        return number;
    }
}
