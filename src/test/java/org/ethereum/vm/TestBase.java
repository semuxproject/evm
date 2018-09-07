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

import java.math.BigInteger;
import java.util.Arrays;

import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.BlockStoreMock;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.client.RepositoryMock;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvokeImpl;
import org.junit.After;
import org.junit.Before;

public class TestBase {

    protected final byte[] address = address(1);
    protected final byte[] origin = address(2);
    protected final byte[] caller = address(2);
    protected final long gas = 1_000_00L;
    protected final BigInteger gasPrice = BigInteger.ONE;
    protected final BigInteger value = BigInteger.ZERO;
    protected final byte[] data = new byte[0];

    protected final byte[] prevHash = new byte[32];
    protected final byte[] coinbase = address(3);
    protected final long timestamp = System.currentTimeMillis();
    protected final long number = 1;
    protected final BigInteger difficulty = BigInteger.TEN;
    protected final long gasLimit = 10_000_000L;

    protected final int callDepth = 0;
    protected final boolean isStaticCall = false;

    protected Repository repository;
    protected BlockStore blockStore;

    protected ProgramInvokeImpl invoke;
    protected Program program;

    @Before
    public void setup() {
        this.repository = new RepositoryMock();
        this.blockStore = new BlockStoreMock();

        this.invoke = new ProgramInvokeImpl(
                new DataWord(address),
                new DataWord(origin),
                new DataWord(caller),
                new DataWord(gas),
                new DataWord(gasPrice),
                new DataWord(value),
                data,
                new DataWord(prevHash),
                new DataWord(coinbase),
                new DataWord(timestamp),
                new DataWord(number),
                new DataWord(difficulty),
                new DataWord(gasLimit),
                repository,
                blockStore,
                callDepth,
                isStaticCall);
    }

    @After
    public void tearDown() {
    }

    public byte[] address(int n) {
        byte[] a = new byte[20];
        Arrays.fill(a, (byte) n);
        return a;
    }
}
