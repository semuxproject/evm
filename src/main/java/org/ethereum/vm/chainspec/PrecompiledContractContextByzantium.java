package org.ethereum.vm.chainspec;

import org.ethereum.vm.client.PrecompiledContractContext;

/**
 * Byzantium's precompiled contracts do not require any additional context, so
 * this class is a no-op.
 */
public class PrecompiledContractContextByzantium implements PrecompiledContractContext {
    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
