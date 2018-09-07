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
package org.ethereum.vm.client;

import java.util.Arrays;
import java.util.List;

import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;
import org.ethereum.vm.util.ByteArrayWrapper;

public class TransactionSummary {

    private Transaction tx;

    private boolean isFailed;
    private long gasUsed;
    private byte[] returnData;
    private List<InternalTransaction> internalTransactions;
    private List<ByteArrayWrapper> deletedAccounts;
    private List<LogInfo> logs;

    public TransactionSummary(Transaction tx, boolean isFailed, long gasUsed, byte[] returnData,
            List<InternalTransaction> internalTransactions,
            List<ByteArrayWrapper> deletedAccounts,
            List<LogInfo> logs) {
        this.tx = tx;
        this.gasUsed = gasUsed;
        this.isFailed = isFailed;
        this.returnData = returnData;
        this.internalTransactions = internalTransactions;
        this.deletedAccounts = deletedAccounts;
        this.logs = logs;
    }

    public Transaction getTx() {
        return tx;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public List<ByteArrayWrapper> getDeletedAccounts() {
        return deletedAccounts;
    }

    public List<LogInfo> getLogs() {
        return logs;
    }

    @Override
    public String toString() {
        return "TransactionSummary{" +
                "isFailed=" + isFailed +
                ", gasUsed=" + gasUsed +
                ", returnData=" + Arrays.toString(returnData) +
                ", internalTransactions=" + internalTransactions +
                ", deletedAccounts=" + deletedAccounts +
                ", logs=" + logs +
                '}';
    }
}
