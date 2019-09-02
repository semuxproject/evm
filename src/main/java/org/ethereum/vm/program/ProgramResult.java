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
package org.ethereum.vm.program;

import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ethereum.vm.LogInfo;
import org.ethereum.vm.util.ByteArrayWrapper;

/**
 * A data structure to hold the results of program.
 */
public class ProgramResult {

    private long gasLimit; // immutable

    private long gasUsed = 0;
    private byte[] returnData = EMPTY_BYTE_ARRAY;
    private RuntimeException exception = null;
    private boolean revert = false;

    // fields below can be merged
    private List<InternalTransaction> internalTransactions = new ArrayList<>();
    private Set<ByteArrayWrapper> deleteAccounts = new HashSet<>();
    private List<LogInfo> logs = new ArrayList<>();
    private long futureRefund = 0;

    private ProgramResult(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public long getGasLeft() {
        return gasLimit - gasUsed;
    }

    public void spendGas(long gas) {
        gasUsed += gas;
    }

    public void refundGas(long gas) {
        gasUsed -= gas;
    }

    public void drainGas() {
        gasUsed = gasLimit;
    }

    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;

        // if there is an exception, the return data should always be EMPTY
        if (exception != null) {
            this.returnData = EMPTY_BYTE_ARRAY;
        }
    }

    public void setRevert(boolean isRevert) {
        this.revert = isRevert;
    }

    public boolean isRevert() {
        return revert;
    }

    public Set<ByteArrayWrapper> getDeleteAccounts() {
        return deleteAccounts;
    }

    public void addDeleteAccount(byte[] address) {
        deleteAccounts.add(new ByteArrayWrapper(address));
    }

    public void addDeleteAccounts(Set<ByteArrayWrapper> accounts) {
        deleteAccounts.addAll(accounts);
    }

    public List<LogInfo> getLogs() {
        return logs;
    }

    public void addLogInfo(LogInfo log) {
        logs.add(log);
    }

    public void addLogs(List<LogInfo> logs) {
        for (LogInfo log : logs) {
            addLogInfo(log);
        }
    }

    public void setLogs(List<LogInfo> logs) {
        this.logs = logs;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public void addInternalTransaction(InternalTransaction tx) {
        internalTransactions.add(tx);
    }

    public void addInternalTransactions(List<InternalTransaction> txs) {
        internalTransactions.addAll(txs);
    }

    public void setInternalTransactions(List<InternalTransaction> txs) {
        this.internalTransactions = txs;
    }

    public void rejectInternalTransactions() {
        for (InternalTransaction internalTx : internalTransactions) {
            internalTx.reject();
        }
    }

    public void addFutureRefund(long gasValue) {
        futureRefund += gasValue;
    }

    public long getFutureRefund() {
        return futureRefund;
    }

    public void resetFutureRefund() {
        futureRefund = 0;
    }

    public void merge(ProgramResult another) {
        addInternalTransactions(another.getInternalTransactions());

        if (another.getException() == null && !another.isRevert()) {
            addDeleteAccounts(another.getDeleteAccounts());
            addLogs(another.getLogs());
            addFutureRefund(another.getFutureRefund());
        }
    }

    public static ProgramResult createEmptyResult(long gasLimit) {
        ProgramResult result = new ProgramResult(gasLimit);
        return result;
    }

    public static ProgramResult createExceptionResult(long gasLimit, RuntimeException exception) {
        ProgramResult result = new ProgramResult(gasLimit);
        result.setException(exception);
        result.drainGas();
        return result;
    }
}
