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

import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;
import org.ethereum.vm.util.ByteArrayWrapper;

public class TransactionReceipt {

    private Transaction tx;

    private boolean success;
    private long gasUsed;
    private byte[] returnData;
    private List<LogInfo> logs;

    // transient
    private List<ByteArrayWrapper> deletedAccounts;
    private List<InternalTransaction> internalTransactions;

    public TransactionReceipt(Transaction tx, boolean success, long gasUsed, byte[] returnData, List<LogInfo> logs,
            List<ByteArrayWrapper> deletedAccounts, List<InternalTransaction> internalTransactions) {
        this.tx = tx;
        this.success = success;
        this.gasUsed = gasUsed;
        this.returnData = returnData;
        this.logs = logs;
        this.deletedAccounts = deletedAccounts;
        this.internalTransactions = internalTransactions;
    }

    public Transaction getTx() {
        return tx;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public List<LogInfo> getLogs() {
        return logs;
    }

    public List<ByteArrayWrapper> getDeletedAccounts() {
        return deletedAccounts;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (InternalTransaction tx : internalTransactions) {
            sb.append("\n|--").append(tx.toString());
        }

        return "TransactionReceipt{"
                + "success=" + success
                + ", gasUsed=" + gasUsed
                + ", returnData=" + Hex.toHexString(returnData)
                + ", deletedAccounts=" + deletedAccounts + ", "
                + "logs=" + logs + "}"
                + sb.toString();
    }
}
