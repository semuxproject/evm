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

import static org.ethereum.vm.util.BigIntegerUtil.isNotCovers;
import static org.ethereum.vm.util.BigIntegerUtil.transfer;
import static org.ethereum.vm.util.ByteArrayUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.vm.util.ByteArrayUtil.getLength;
import static org.ethereum.vm.util.ByteArrayUtil.isEmpty;
import static org.ethereum.vm.util.ByteArrayUtil.isNotEmpty;
import static org.ethereum.vm.util.ByteArrayUtil.nullToEmpty;

import java.math.BigInteger;
import java.util.Arrays;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.MessageCall;
import org.ethereum.vm.OpCode;
import org.ethereum.vm.VM;
import org.ethereum.vm.chainspec.PrecompiledContract;
import org.ethereum.vm.chainspec.PrecompiledContractContext;
import org.ethereum.vm.chainspec.Spec;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.program.exception.BytecodeExecutionException;
import org.ethereum.vm.program.exception.CallTooDeepException;
import org.ethereum.vm.program.exception.ExceptionFactory;
import org.ethereum.vm.program.exception.InsufficientBalanceException;
import org.ethereum.vm.program.exception.OutOfGasException;
import org.ethereum.vm.program.exception.PrecompiledFailureException;
import org.ethereum.vm.program.exception.StackUnderflowException;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import org.ethereum.vm.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program {

    private static final Logger logger = LoggerFactory.getLogger(Program.class);

    /**
     * This attribute defines the number of recursive calls allowed in the EVM Note:
     * For the JVM to reach this level without a StackOverflow exception, ethereumj
     * may need to be started with a JVM argument to increase the stack size. For
     * example: -Xss10m
     */
    private static final int MAX_DEPTH = 1024;

    // Max size for stack checks
    private static final int MAX_STACKSIZE = 1024;

    private ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();
    private ProgramInvoke invoke;
    private ProgramResult result;

    private Spec spec;
    private ProgramPreprocess preprocessed;

    private Stack stack;
    private Memory memory;
    private Repository repo;
    private Repository originalRepo;
    private byte[] returnDataBuffer;

    private byte[] ops;
    private int pc;
    private boolean stopped;

    public Program(byte[] ops, ProgramInvoke programInvoke, Spec spec) {
        this.ops = nullToEmpty(ops);
        this.invoke = programInvoke;
        this.result = ProgramResult.createEmptyResult(invoke.getGasLimit());

        this.memory = new Memory();
        this.stack = new Stack();
        this.repo = programInvoke.getRepository();
        this.originalRepo = programInvoke.getOriginalRepository();

        this.spec = spec;
    }

    public Program(byte[] ops, ProgramInvoke programInvoke) {
        this(ops, programInvoke, Spec.DEFAULT);
    }

    public ProgramPreprocess getProgramPreprocess() {
        if (preprocessed == null) {
            preprocessed = ProgramPreprocess.compile(ops);
        }
        return preprocessed;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasLeft() {
        return getResult().getGasLeft();
    }

    public long getGasUsed() {
        return getResult().getGasUsed();
    }

    private InternalTransaction addInternalTx(OpCode type, byte[] from, byte[] to, long nonce, BigInteger value,
            byte[] data, long gas) {

        int depth = getCallDepth();
        int index = getResult().getInternalTransactions().size();

        InternalTransaction tx = new InternalTransaction(depth, index, type,
                from, to, nonce, value, data, gas, getGasPrice().value());
        getResult().addInternalTransaction(tx);

        return tx;
    }

    public byte getCurrentOp() {
        return isEmpty(ops) ? 0 : ops[pc];
    }

    public void stackPush(byte[] data) {
        stackPush(DataWord.of(data));
    }

    public void stackPushZero() {
        stackPush(DataWord.ZERO);
    }

    public void stackPushOne() {
        stackPush(DataWord.ONE);
    }

    public void stackPush(DataWord stackWord) {
        verifyStackOverflow(0, 1); // Sanity Check
        stack.push(stackWord);
    }

    public Stack getStack() {
        return this.stack;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(int pc) {
        this.pc = pc;

        if (this.pc >= ops.length) {
            stop();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setHReturn(byte[] buff) {
        getResult().setReturnData(buff);
    }

    public void stop() {
        stopped = true;
    }

    public void step() {
        setPC(pc + 1);
    }

    public byte[] sweep(int n) {

        if (pc + n > ops.length)
            stop();

        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        if (pc >= ops.length)
            stop();

        return data;
    }

    public DataWord stackPop() {
        return stack.pop();
    }

    /**
     * Verifies that the stack is at least <code>stackSize</code>
     *
     * @param stackSize
     *            int
     * @throws StackUnderflowException
     *             If the stack is smaller than <code>stackSize</code>
     */
    public void verifyStackUnderflow(int stackSize) throws StackUnderflowException {
        if (stack.size() < stackSize) {
            throw ExceptionFactory.tooSmallStack(stackSize, stack.size());
        }
    }

    public void verifyStackOverflow(int argsReqs, int returnReqs) {
        if ((stack.size() - argsReqs + returnReqs) > MAX_STACKSIZE) {
            throw ExceptionFactory.tooLargeStack((stack.size() - argsReqs + returnReqs), MAX_STACKSIZE);
        }
    }

    public int getMemSize() {
        return memory.size();
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memory.write(addrB.intValue(), value.getData(), value.getData().length, false);
    }

    public void memorySaveLimited(int addr, byte[] data, int dataSize) {
        memory.write(addr, data, dataSize, true);
    }

    public void memorySave(int addr, byte[] value) {
        memory.write(addr, value, value.length, false);
    }

    public void memoryExpand(DataWord outDataOffs, DataWord outDataSize) {
        if (!outDataSize.isZero()) {
            memory.extend(outDataOffs.intValue(), outDataSize.intValue());
        }
    }

    /**
     * Allocates a piece of memory and stores value at given offset address
     *
     * @param addr
     *            is the offset address
     * @param allocSize
     *            size of memory needed to write
     * @param value
     *            the data to write to memory
     */
    public void memorySave(int addr, int allocSize, byte[] value) {
        memory.extendAndWrite(addr, allocSize, value);
    }

    public DataWord memoryLoad(DataWord addr) {
        return memory.readWord(addr.intValue());
    }

    public byte[] memoryChunk(int offset, int size) {
        return memory.read(offset, size);
    }

    /**
     * Allocates extra memory in the program for a specified size, calculated from a
     * given offset
     *
     * @param offset
     *            the memory address offset
     * @param size
     *            the number of bytes to allocate
     */
    public void allocateMemory(int offset, int size) {
        memory.extend(offset, size);
    }

    public void suicide(DataWord beneficiary) {
        byte[] owner = getOwnerAddress().getLast20Bytes();
        byte[] obtainer = beneficiary.getLast20Bytes();
        BigInteger balance = getRepository().getBalance(owner);

        addInternalTx(OpCode.SUICIDE, owner, obtainer, getRepository().getNonce(owner), balance,
                EMPTY_BYTE_ARRAY, 0);

        if (Arrays.equals(owner, obtainer)) {
            // if owner == obtainer just zeroing account according to Yellow Paper
            getRepository().addBalance(owner, balance.negate());
        } else {
            transfer(getRepository(), owner, obtainer, balance);
        }

        getResult().addDeleteAccount(this.getOwnerAddress().getLast20Bytes());
    }

    public Repository getRepository() {
        return this.repo;
    }

    public Repository getOriginalRepository() {
        return this.originalRepo;
    }

    /**
     * Create contract for {@link OpCode#CREATE}
     */
    public ProgramResult createContract(DataWord value, DataWord memStart, DataWord memSize) {
        resetReturnDataBuffer();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        RuntimeException exception = verifyCall(senderAddress, endowment);
        if (exception != null) {
            // in case of insufficient balance or call is too deep,
            // throw an exception
            return ProgramResult.createExceptionResult(getGasLeft(), exception);
        }

        long nonce = getRepository().getNonce(senderAddress);
        byte[] contractAddress = HashUtil.calcNewAddress(senderAddress, nonce);
        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());

        ProgramResult callResult = createContractImpl(value, programCode, contractAddress);
        setReturnDataBuffer(callResult.getReturnData());
        return callResult;
    }

    /**
     * Create contract for {@link OpCode#CREATE2}
     */
    public ProgramResult createContract2(DataWord value, DataWord memStart, DataWord memSize, DataWord salt) {
        resetReturnDataBuffer();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        RuntimeException exception = verifyCall(senderAddress, endowment);
        if (exception != null) {
            // in case of insufficient balance or call is too deep,
            // throw an exception
            return ProgramResult.createExceptionResult(getGasLeft(), exception);
        }

        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());
        byte[] contractAddress = HashUtil.calcSaltAddress(senderAddress, programCode, salt.getData());

        ProgramResult callResult = createContractImpl(value, programCode, contractAddress);
        setReturnDataBuffer(callResult.getReturnData());
        return callResult;
    }

    /**
     * Call a contract for {@link OpCode#CALL}, {@link OpCode#CALLCODE} or
     * {@link OpCode#DELEGATECALL}.
     */
    public ProgramResult callContract(OpCode type, long gas, DataWord codeAddress, DataWord value, DataWord inDataOffs,
            DataWord inDataSize, DataWord outDataOffs, DataWord outDataSize) {
        resetReturnDataBuffer();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        RuntimeException exception = verifyCall(senderAddress, endowment);
        if (exception != null) {
            // in case of insufficient balance or call is too deep,
            // do nothing and refund the remaining gas
            refundGas(gas, "call revoked");
            return ProgramResult.createEmptyResult(gas);
        }

        MessageCall msg = new MessageCall(type, gas, codeAddress, value, inDataOffs, inDataSize,
                outDataOffs, outDataSize);
        PrecompiledContract contract = spec.getPrecompiledContracts().getContractForAddress(codeAddress);

        ProgramResult callResult = callContractImpl(msg, contract);
        setReturnDataBuffer(callResult.getReturnData());
        return callResult;
    }

    /**
     * All stages required to create contract on provided address after initial
     * check
     *
     * @param value
     *            Endowment
     * @param programCode
     *            Contract code
     * @param newAddress
     *            Contract address
     */
    private ProgramResult createContractImpl(DataWord value, byte[] programCode, byte[] newAddress) {
        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        boolean contractAlreadyExists = getRepository().exists(newAddress);

        // [1] SPEND GAS
        long gas = spec.getCreateGas(getGasLeft());
        spendGas(gas, "internal call - create");

        // [2] UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        getRepository().increaseNonce(senderAddress);

        // track for reversibility when failure
        Repository track = getRepository().startTracking();

        // [3] SET THE NONCE OF NEW CONTRACT TO ONE?????
        track.increaseNonce(newAddress);

        // [4] TRANSFER THE BALANCE
        transfer(track, senderAddress, newAddress, value.value());

        // [5] COOK AN INTERNAL TRANSACTION
        InternalTransaction internalTx = addInternalTx(OpCode.CREATE, senderAddress, EMPTY_BYTE_ARRAY,
                getRepository().getNonce(senderAddress), value.value(), programCode, gas);
        if (logger.isDebugEnabled()) {
            logger.debug("CREATE: {}", internalTx);
        }

        // [6] EXECUTE THE CODE
        ProgramResult result;
        if (contractAlreadyExists) {
            result = ProgramResult.createExceptionResult(gas,
                    new BytecodeExecutionException("Account already exists: 0x"
                            + HexUtil.toHexString(newAddress)));
        } else if (isNotEmpty(programCode)) {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(this,
                    getOwnerAddress(),
                    DataWord.of(newAddress),
                    gas,
                    value,
                    EMPTY_BYTE_ARRAY,
                    track,
                    this.invoke.getBlockStore(),
                    false);
            Program program = new Program(programCode, programInvoke, spec);

            new VM(spec).play(program);
            result = program.getResult();
        } else {
            result = ProgramResult.createEmptyResult(gas);
        }

        // [7] POST EXECUTION PROCESSING
        if (result.getException() == null && !result.isRevert()) {
            byte[] code = result.getReturnData();
            long storageCost = getLength(code) * spec.getFeeSchedule().getCREATE_DATA();

            if (result.getGasLeft() < storageCost) {
                result.setReturnData(EMPTY_BYTE_ARRAY);
                if (!spec.createEmptyContractOnOOG()) {
                    result.setException(ExceptionFactory.notEnoughSpendingGas("No gas to return just created contract",
                            storageCost, this));
                } else {
                    track.saveCode(newAddress, EMPTY_BYTE_ARRAY);
                }
            } else if (getLength(code) > spec.maxContractSize()) {
                result.setReturnData(EMPTY_BYTE_ARRAY);
                result.setException(ExceptionFactory.notEnoughSpendingGas("Contract size too large: "
                        + getLength(result.getReturnData()), storageCost, this));
            } else {
                result.spendGas(storageCost);
                track.saveCode(newAddress, code);
            }

            track.commit();

            // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
            stackPush(DataWord.of(newAddress));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Contract run halted by Exception: contract: [{}], exception: [{}]",
                        HexUtil.toHexString(newAddress),
                        result.getException());
            }

            internalTx.reject();
            result.rejectInternalTransactions();

            track.rollback();
            stackPushZero();
        }

        // [8] REFUND THE REMAIN GAS
        if (result.getException() == null) {
            long refundGas = result.getGasLeft();
            if (refundGas > 0) {
                refundGas(refundGas, "remaining gas from create");
            }
        }

        // [9] MERGE RESULT INTO PARENT
        getResult().merge(result);

        return result;
    }

    /**
     * Makes an internal call to another address.
     *
     * Note: normal calls invoke a specified contract which updates itself, while
     * Stateless calls invoke code from another contract, within the context of the
     * caller.
     *
     * @param msg
     *            the message call object
     * @param contract
     *            the called precompiled contract, can be NULL
     */
    private ProgramResult callContractImpl(MessageCall msg, PrecompiledContract contract) {
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] senderAddress = getOwnerAddress().getLast20Bytes();
        byte[] contextAddress = msg.getType().callIsStateless() ? senderAddress : codeAddress;
        byte[] data = memoryChunk(msg.getInDataOffs().intValue(), msg.getInDataSize().intValue());
        BigInteger endowment = msg.getEndowment().value();

        // track for reversibility when failure
        Repository track = getRepository().startTracking();

        // [4] TRANSFER THE BALANCE
        transfer(track, senderAddress, contextAddress, endowment);

        // [5] COOK AN INTERNAL TRANSACTION AND INVOKE
        InternalTransaction internalTx = addInternalTx(msg.getType(), senderAddress, contextAddress,
                getRepository().getNonce(senderAddress), endowment, data, msg.getGas());
        if (logger.isDebugEnabled()) {
            logger.debug("CALL: {}", internalTx);
        }

        // [6] EXECUTE THE CODE
        ProgramResult result;
        if (contract != null) {
            long requiredGas = contract.getGasForData(data);
            if (requiredGas > msg.getGas()) {
                track.rollback();
                stackPushZero();
                result = ProgramResult.createExceptionResult(msg.getGas(),
                        new OutOfGasException("Precompiled out-of-gas"));
            } else {
                Pair<Boolean, byte[]> out = contract.execute(new PrecompiledContractContext() {
                    @Override
                    public Repository getTrack() {
                        return track;
                    }

                    @Override
                    public byte[] getCaller() {
                        return senderAddress;
                    }

                    @Override
                    public BigInteger getValue() {
                        return endowment;
                    }

                    @Override
                    public byte[] getData() {
                        return data;
                    }
                });
                result = ProgramResult.createEmptyResult(msg.getGas());
                result.spendGas(requiredGas);
                if (!out.getLeft()) {
                    result.setReturnData(EMPTY_BYTE_ARRAY);
                    result.setException(new PrecompiledFailureException());
                } else {
                    result.setReturnData(out.getRight());
                }
            }
        } else {
            byte[] programCode = getRepository().getCode(codeAddress);
            if (isNotEmpty(programCode)) {
                ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(this,
                        msg.getType().callIsDelegate() ? getCallerAddress() : getOwnerAddress(),
                        DataWord.of(contextAddress),
                        msg.getGas(),
                        msg.getType().callIsDelegate() ? getCallValue() : msg.getEndowment(),
                        data,
                        track,
                        this.invoke.getBlockStore(),
                        msg.getType().callIsStatic() || isStaticCall());
                Program program = new Program(programCode, programInvoke, spec);
                new VM(spec).play(program);
                result = program.getResult();
            } else {
                result = ProgramResult.createEmptyResult(msg.getGas());
            }
        }

        // [7] POST EXECUTION PROCESSING
        if (result.getException() == null && !result.isRevert()) {
            track.commit();

            // IN SUCCESS PUSH ONE INTO THE STACK
            stackPushOne();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                        HexUtil.toHexString(contextAddress),
                        result.getException());
            }

            internalTx.reject();
            result.rejectInternalTransactions();

            track.rollback();
            stackPushZero();
        }

        // APPLY RESULTS: result.getReturnData() into out_memory allocated
        if (isNotEmpty(result.getReturnData())) {
            byte[] buffer = result.getReturnData();
            int offset = msg.getOutDataOffs().intValue();
            int size = msg.getOutDataSize().intValue();

            memorySaveLimited(offset, buffer, size);
        }

        // [8] REFUND THE REMAIN GAS
        if (result.getException() == null) {
            long refundGas = result.getGasLeft();
            if (refundGas > 0) {
                refundGas(refundGas, "remaining gas from call");
            }
        }

        // [9] MERGE RESULT INTO PARENT
        getResult().merge(result);

        return result;
    }

    public void spendGas(long gasValue, String cause) {
        logger.debug("Spend: cause = [{}], gas = [{}]", cause, gasValue);

        if (getGasLeft() < gasValue) {
            throw ExceptionFactory.notEnoughSpendingGas(cause, gasValue, this);
        }
        getResult().spendGas(gasValue);
    }

    public void spendAllGas() {
        spendGas(getGasLeft(), "consume all");
    }

    public void refundGas(long gasValue, String cause) {
        logger.debug("Refund: cause = [{}], gas = [{}]", cause, gasValue);

        getResult().refundGas(gasValue);
    }

    public void futureRefundGas(long gasValue) {
        logger.debug("Future refund added: [{}]", gasValue);

        getResult().addFutureRefund(gasValue);
    }

    public void resetFutureRefund() {
        getResult().resetFutureRefund();
    }

    public void storageSave(DataWord key, DataWord value) {
        getRepository().putStorageRow(getOwnerAddress().getLast20Bytes(), key, value);
    }

    public byte[] getCode() {
        return ops;
    }

    public byte[] getCodeAt(DataWord address) {
        byte[] code = invoke.getRepository().getCode(address.getLast20Bytes());
        return nullToEmpty(code);
    }

    public DataWord getOwnerAddress() {
        return invoke.getOwnerAddress();
    }

    public DataWord getBlockHash(int index) {
        return index < this.getBlockNumber().longValue()
                && index >= Math.max(256, this.getBlockNumber().intValue()) - 256
                        ? DataWord.of(this.invoke.getBlockStore().getBlockHashByNumber(index))
                        : DataWord.ZERO;
    }

    public DataWord getBalance(DataWord address) {
        BigInteger balance = getRepository().getBalance(address.getLast20Bytes());
        return DataWord.of(balance.toByteArray());
    }

    public DataWord getOriginAddress() {
        return invoke.getOriginAddress();
    }

    public DataWord getCallerAddress() {
        return invoke.getCallerAddress();
    }

    public DataWord getGasPrice() {
        return invoke.getGasPrice();
    }

    public DataWord getCallValue() {
        return invoke.getValue();
    }

    public DataWord getDataSize() {
        return invoke.getDataSize();
    }

    public DataWord getDataValue(DataWord index) {
        return invoke.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        return invoke.getDataCopy(offset, length);
    }

    public DataWord getReturnDataBufferSize() {
        return DataWord.of(getReturnDataBufferSizeI());
    }

    public byte[] getReturnDataBufferData(DataWord off, DataWord size) {
        if ((long) off.intValueSafe() + size.intValueSafe() > getReturnDataBufferSizeI())
            return null;
        return returnDataBuffer == null ? EMPTY_BYTE_ARRAY
                : Arrays.copyOfRange(returnDataBuffer, off.intValueSafe(), off.intValueSafe() + size.intValueSafe());
    }

    /**
     * Returns the current storage data for key
     */
    public DataWord getCurrentStorageValue(DataWord key) {
        return getRepository().getStorageRow(getOwnerAddress().getLast20Bytes(), key);
    }

    /**
     * Returns the storage data at the beginning of program execution
     */
    public DataWord getOriginalStorageValue(DataWord key) {
        return getOriginalRepository().getStorageRow(getOwnerAddress().getLast20Bytes(), key);
    }

    public DataWord getBlockCoinbase() {
        return invoke.getBlockCoinbase();
    }

    public DataWord getBlockTimestamp() {
        return invoke.getBlockTimestamp();
    }

    public DataWord getBlockNumber() {
        return invoke.getBlockNumber();
    }

    public DataWord getBlockDifficulty() {
        return invoke.getBlockDifficulty();
    }

    public DataWord getBlockGasLimit() {
        return invoke.getBlockGasLimit();
    }

    public DataWord getBlockPrevHash() {
        return invoke.getBlockPrevHash();
    }

    public int getCallDepth() {
        return invoke.getCallDepth();
    }

    public boolean isStaticCall() {
        return invoke.isStaticCall();
    }

    public void setException(RuntimeException e) {
        getResult().setException(e);
    }

    public void setRevert(boolean isRevert) {
        getResult().setRevert(isRevert);
    }

    public int verifyJumpDest(DataWord nextPC) {
        if (nextPC.bytesOccupied() > 4) {
            throw ExceptionFactory.badJumpDestination(-1);
        }
        int ret = nextPC.intValue();
        if (!getProgramPreprocess().hasJumpDest(ret)) {
            throw ExceptionFactory.badJumpDestination(ret);
        }
        return ret;
    }

    /**
     * used mostly for testing reasons
     */
    public byte[] getMemory() {
        return memory.read(0, memory.size());
    }

    /**
     * used mostly for testing reasons
     */
    public void initMem(byte[] data) {
        this.memory.write(0, data, data.length, false);
    }

    private void resetReturnDataBuffer() {
        returnDataBuffer = null;
    }

    private void setReturnDataBuffer(byte[] newReturnData) {
        returnDataBuffer = newReturnData;
    }

    private int getReturnDataBufferSizeI() {
        return returnDataBuffer == null ? 0 : returnDataBuffer.length;
    }

    private RuntimeException verifyCall(byte[] senderAddress, BigInteger endowment) {
        if (getCallDepth() == MAX_DEPTH) {
            stackPushZero();
            return new CallTooDeepException();
        }

        if (isNotCovers(getRepository().getBalance(senderAddress), endowment)) {
            stackPushZero();
            return new InsufficientBalanceException();
        }

        return null;
    }
}
