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
package org.ethereum.vm.compliance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.vm.client.*;
import org.ethereum.vm.compliance.spec.Address;
import org.ethereum.vm.compliance.spec.Environment;
import org.ethereum.vm.compliance.spec.Exec;
import org.ethereum.vm.compliance.spec.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EthereumComplianceTest {

    private static Logger logger = LoggerFactory.getLogger(EthereumComplianceTest.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void runAllTests() throws IOException {

        TypeReference<HashMap<String, TestCase>> typeRef = new TypeReference<HashMap<String, TestCase>>() {
        };

        File rootTestDirectory = new File("./src/test/resources/tests/VMTests");
        for (File file : rootTestDirectory.listFiles()) {
            if (file.isDirectory()) {
                for (File test : file.listFiles()) {
                    if (test.isFile()) {
                        HashMap<String, TestCase> suite = objectMapper.readValue(test, typeRef);
                        for (Map.Entry<String, TestCase> testCase : suite.entrySet()) {
                            String testName = file.getName() + " - " + testCase.getKey();
                            runTest(testName, testCase.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * run a VM test
     *
     * @param testName
     * @param testCase
     */
    private void runTest(String testName, TestCase testCase) {
        logger.info("Running " + testName);

        Transaction transaction = buildTransaction(testCase.getExec());
        Block block = buildBlock(testCase.getEnvironment());
        Repository repository = buildRepository(testCase.getPre());
        BlockStore blockStore = buildBlockStore();

        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        Repository expectedRepository = buildRepository(testCase.getPost());

        Assert.assertEquals(expectedRepository, repository);
    }

    private BlockStore buildBlockStore() {

        return new BlockStoreMock();
    }

    private Repository buildRepository(Map<String, Address> pre) {
        RepositoryMock repository = new RepositoryMock();
        return repository;
    }

    private Transaction buildTransaction(Exec exec) {
        return null;
    }

    private Block buildBlock(Environment environment) {
        return null;
    }
}
