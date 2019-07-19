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

import org.ethereum.vm.FeeSchedule;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.client.Transaction;
import org.ethereum.vm.program.exception.OutOfGasException;

/**
 * An abstract implementation of the chain spec, based on the Homestead code
 * base.
 *
 *
 * Ethereum forks in history:
 *
 * <ul>
 * <li>Frontier</li>
 * <li>Ice Age</li>
 * <li>Homestead</li>
 * <li>DAO</li>
 * <li>Tangerine Whistle (EIP-150: IO-opcode gas changes, max call/create gas,
 * EIP-158: state clearing)</li>
 * <li>Spurious Dragon (EIP-160: EXP cost increase, EIP-161: State trie
 * clearing, EIP-170 contract size limit)</li>
 * <li>Byzantium</li>
 * <li>Constantinople</li>
 * </ul>
 */
public class BaseSpec implements Spec {
    private static final FeeSchedule feeSchedule = new FeeSchedule();
    private static final PrecompiledContracts precompiledContracts = new BasePrecompiledContracts();

    @Override
    public FeeSchedule getFeeSchedule() {
        return feeSchedule;
    }

    @Override
    public PrecompiledContracts getPrecompiledContracts() {
        return precompiledContracts;
    }

    @Override
    public long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException {
        return availableGas;
    }

    @Override
    public long getCreateGas(long availableGas) {
        return availableGas;
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        FeeSchedule fs = getFeeSchedule();

        long cost = tx.isCreate() ? fs.getTRANSACTION_CREATE_CONTRACT() : fs.getTRANSACTION();
        for (byte b : tx.getData()) {
            cost += (b == 0) ? fs.getTX_ZERO_DATA() : fs.getTX_NO_ZERO_DATA();
        }

        return cost;
    }

    @Override
    public int maxContractSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean createEmptyContractOnOOG() {
        return false;
    }

    @Override
    public boolean eip1052() {
        return false;
    }

    @Override
    public boolean eip145() {
        return false;
    }

    @Override
    public boolean eip1283() {
        return false;
    }

    @Override
    public boolean eip1014() {
        return false;
    }
}
