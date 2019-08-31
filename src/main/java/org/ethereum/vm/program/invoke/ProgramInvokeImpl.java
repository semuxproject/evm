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

import java.math.BigInteger;
import java.util.Objects;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.util.HexUtil;

public class ProgramInvokeImpl implements ProgramInvoke {

    /**
     * Transaction environment
     */
    private final DataWord address, origin, caller, gasPrice, value;
    private final byte[] data;
    private final long gasLimit;

    /**
     * Block environment
     */
    private final DataWord blockPrevHash, blockCoinbase, blockTimestamp, blockNumber, blockDifficulty, blockGasLimit;

    /**
     * Database environment
     */
    private final Repository repository;
    private final Repository originalRepository;
    private final BlockStore blockStore;

    private int callDepth;
    private boolean isStaticCall;

    public ProgramInvokeImpl(DataWord address, DataWord origin, DataWord caller,
            long gas, DataWord gasPrice, DataWord value, byte[] data, DataWord blockPrevHash,
            DataWord blockCoinbase, DataWord blockTimestamp, DataWord blockNumber, DataWord blockDifficulty,
            DataWord gasLimit, Repository repository, Repository originalRepository, BlockStore blockStore,
            int callDepth, boolean isStaticCall) {

        Objects.requireNonNull(address);
        Objects.requireNonNull(origin);
        Objects.requireNonNull(caller);
        Objects.requireNonNull(gasPrice);
        Objects.requireNonNull(value);
        Objects.requireNonNull(data);

        Objects.requireNonNull(blockPrevHash);
        Objects.requireNonNull(blockCoinbase);
        Objects.requireNonNull(blockTimestamp);
        Objects.requireNonNull(blockNumber);
        Objects.requireNonNull(blockDifficulty);
        Objects.requireNonNull(gasLimit);

        Objects.requireNonNull(repository);
        Objects.requireNonNull(blockStore);

        this.address = address;
        this.origin = origin;
        this.caller = caller;
        this.gasLimit = gas;
        this.gasPrice = gasPrice;
        this.value = value;
        this.data = data;

        this.blockPrevHash = blockPrevHash;
        this.blockCoinbase = blockCoinbase;
        this.blockTimestamp = blockTimestamp;
        this.blockNumber = blockNumber;
        this.blockDifficulty = blockDifficulty;
        this.blockGasLimit = gasLimit;

        this.repository = repository;
        this.originalRepository = originalRepository;
        this.blockStore = blockStore;

        this.callDepth = callDepth;
        this.isStaticCall = isStaticCall;
    }

    @Override
    public DataWord getOwnerAddress() {
        return address;
    }

    @Override
    public DataWord getOriginAddress() {
        return origin;
    }

    @Override
    public DataWord getCallerAddress() {
        return caller;
    }

    @Override
    public long getGasLimit() {
        return gasLimit;
    }

    @Override
    public DataWord getGasPrice() {
        return gasPrice;
    }

    @Override
    public DataWord getValue() {
        return value;
    }

    // open for testing
    public byte[] getData() {
        return data;
    }

    @Override
    public DataWord getDataValue(DataWord indexData) {
        byte[] data = getData();

        BigInteger indexBI = indexData.value();
        if (indexBI.compareTo(BigInteger.valueOf(data.length)) >= 0) {
            return DataWord.ZERO;
        }

        int idx = indexBI.intValue();
        int size = Math.min(data.length - idx, DataWord.SIZE);

        byte[] buffer = new byte[DataWord.SIZE];
        System.arraycopy(data, idx, buffer, 0, size); // left-aligned

        return DataWord.of(buffer);
    }

    @Override
    public DataWord getDataSize() {
        byte[] data = getData();

        return DataWord.of(data.length);
    }

    @Override
    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData) {
        byte[] data = getData();

        BigInteger offsetBI = offsetData.value();
        BigInteger lengthBI = lengthData.value();

        if (offsetBI.compareTo(BigInteger.valueOf(data.length)) >= 0) {
            return new byte[0];
        }

        int offset = offsetBI.intValue();
        int size = data.length - offset;
        if (lengthBI.compareTo(BigInteger.valueOf(size)) < 0) {
            size = lengthBI.intValue();
        }

        byte[] buffer = new byte[size];
        System.arraycopy(data, offset, buffer, 0, size);

        return buffer;
    }

    @Override
    public DataWord getBlockPrevHash() {
        return blockPrevHash;
    }

    @Override
    public DataWord getBlockCoinbase() {
        return blockCoinbase;
    }

    @Override
    public DataWord getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public DataWord getBlockNumber() {
        return blockNumber;
    }

    @Override
    public DataWord getBlockDifficulty() {
        return blockDifficulty;
    }

    @Override
    public DataWord getBlockGasLimit() {
        return blockGasLimit;
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public Repository getOriginalRepository() {
        return originalRepository;
    }

    @Override
    public BlockStore getBlockStore() {
        return blockStore;
    }

    @Override
    public int getCallDepth() {
        return this.callDepth;
    }

    @Override
    public boolean isStaticCall() {
        return isStaticCall;
    }

    @Override
    public String toString() {
        return "ProgramInvokeImpl{" +
                "address=" + address +
                ", origin=" + origin +
                ", caller=" + caller +
                ", gasLimit=" + gasLimit +
                ", gasPrice=" + gasPrice +
                ", value=" + value +
                ", data=" + HexUtil.toHexString(data) +
                ", blockPrevHash=" + blockPrevHash +
                ", blockCoinbase=" + blockCoinbase +
                ", blockTimestamp=" + blockTimestamp +
                ", blockNumber=" + blockNumber +
                ", blockDifficulty=" + blockDifficulty +
                ", blockGasLimit=" + blockGasLimit +
                ", repository=" + repository +
                ", blockStore=" + blockStore +
                ", callDepth=" + callDepth +
                ", isStaticCall=" + isStaticCall +
                '}';
    }
}
