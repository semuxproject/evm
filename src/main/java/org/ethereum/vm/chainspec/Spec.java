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
 * A hub for blockchain specifications.
 */
public interface Spec {

    /**
     * The default specification.
     */
    Spec DEFAULT = new ByzantiumSpec();

    /**
     * Returns the fee schedule.
     *
     * @return
     */
    FeeSchedule getFeeSchedule();

    /**
     * Returns the gas limit for an internal CALL.
     *
     * @param op
     *            the call opcode, e.g. CALL, CALLCODE and DELEGATECALL
     * @param requestedGas
     *            the requested gas
     * @param availableGas
     *            the available gas
     * @return
     * @throws OutOfGasException
     */
    long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException;

    /**
     * Returns the gas limit for an internal CREATE.
     *
     * @param availableGas
     *            the available gas
     * @return
     */
    long getCreateGas(long availableGas);

    /**
     * Returns the basic transaction cost.
     *
     * @param tx
     *            a transaction
     * @return
     */
    long getTransactionCost(Transaction tx);

    /**
     * Returns the max size of a contract.
     *
     * @return
     */
    int maxContractSize();

    /**
     * Whether to create an empty contract or not when running out of gas.
     *
     * @return
     */
    boolean createEmptyContractOnOOG();

    /**
     * EIP1052: https://eips.ethereum.org/EIPS/eip-1052 EXTCODEHASH opcode
     */
    boolean eip1052();

    /**
     * EIP145: https://eips.ethereum.org/EIPS/eip-145 Bitwise shifting instructions
     * in EVM
     */
    boolean eip145();

    /**
     * EIP 1283: https://eips.ethereum.org/EIPS/eip-1283 Net gas metering for SSTORE
     * without dirty maps
     */
    boolean eip1283();

    /**
     * EIP 1014: https://eips.ethereum.org/EIPS/eip-1014 Skinny CREATE2: same as
     * CREATE but with deterministic address
     */
    boolean eip1014();
}
