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
package org.ethereum.vm.config;

import org.ethereum.vm.FeeSchedule;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.client.Transaction;
import org.ethereum.vm.program.exception.OutOfGasException;

public class ByzantiumConfig implements Config {

    private static class FeeScheduleByzantium extends FeeSchedule {
        public int getBALANCE() {
            return 400;
        }

        public int getEXT_CODE_SIZE() {
            return 700;
        }

        public int getEXT_CODE_COPY() {
            return 700;
        }

        public int getSLOAD() {
            return 200;
        }

        public int getCALL() {
            return 700;
        }

        public int getSUICIDE() {
            return 5000;
        }

        public int getNEW_ACCT_SUICIDE() {
            return 25000;
        }

        public int getEXP_BYTE_GAS() {
            return 50;
        }
    }

    private static final FeeSchedule feeSchedule = new FeeScheduleByzantium();
    private static final Constants constants = new Constants();

    public ByzantiumConfig() {

    }

    private static long maxAllowed(long available) {
        return available - available / 64;
    }

    @Override
    public long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException {
        long maxAllowed = maxAllowed(availableGas);
        return requestedGas > maxAllowed ? maxAllowed : requestedGas;
    }

    @Override
    public long getCreateGas(long availableGas) {
        return maxAllowed(availableGas);
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
    public FeeSchedule getFeeSchedule() {
        return feeSchedule;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }
}
