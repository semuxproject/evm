package org.ethereum.vm.client;

/**
 * Class to manage alternate behavior provided by precompiled contracts
 */
public interface PrecompiledContractContext {
    /**
     * Stores all the temporary changes made to the context in the actual
     * database
     */
    void commit();

    /**
     * Undoes all the changes made so far to a snapshot of the context
     */
    void rollback();
}
