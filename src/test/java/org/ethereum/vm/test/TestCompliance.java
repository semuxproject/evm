package org.ethereum.vm.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.vm.client.Block;
import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.BlockStoreMock;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.client.RepositoryMock;
import org.ethereum.vm.client.Transaction;
import org.ethereum.vm.client.TransactionExecutor;
import org.ethereum.vm.client.TransactionReceipt;
import org.ethereum.vm.test.api.Address;
import org.ethereum.vm.test.api.Environment;
import org.ethereum.vm.test.api.Exec;
import org.ethereum.vm.test.api.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestCompliance {

    private static Logger logger = LoggerFactory.getLogger(TestCompliance.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void runAllTests() throws IOException {

        TypeReference<HashMap<String, TestCase>> typeRef = new TypeReference<HashMap<String, TestCase>>() {
        };

        File rootTestDirectory = new File("./src/test/resources/tests");
        for (File file : rootTestDirectory.listFiles()) {
            if (file.isDirectory()) {
                for (File test : file.listFiles()) {
                    if (test.isFile()) {
                        HashMap<String, TestCase> suite = objectMapper.readValue(test, typeRef);
                        for (Map.Entry<String, TestCase> testCase : suite.entrySet()) {
                            String testName = file.getName() + " - " + test.getName() + testCase.getKey();
                            //WIP TODO
//                            runTest(testName, testCase.getValue());
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
        logger.info("running " + testName);

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
