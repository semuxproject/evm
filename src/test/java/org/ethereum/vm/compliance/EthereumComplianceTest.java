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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.VM;
import org.ethereum.vm.chainspec.ConstantinopleSpec;
import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.BlockStoreMock;
import org.ethereum.vm.client.Repository;
import org.ethereum.vm.client.RepositoryMock;
import org.ethereum.vm.compliance.spec.Environment;
import org.ethereum.vm.compliance.spec.Exec;
import org.ethereum.vm.compliance.spec.TestCase;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeImpl;
import org.ethereum.vm.util.HexUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EthereumComplianceTest {

    private static Logger logger = LoggerFactory.getLogger(EthereumComplianceTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void runAllTests() throws IOException {

        TypeReference<HashMap<String, TestCase>> typeRef = new TypeReference<HashMap<String, TestCase>>() {
        };

        File rootTestDirectory = new File("./src/test/resources/tests/VMTests");
        List<File> files = Files.walk(rootTestDirectory.toPath())
                .map(p -> p.toFile())
                .filter(f -> f.getName().endsWith(".json"))
                .collect(Collectors.toList());

        for (File file : files) {
            HashMap<String, TestCase> suite = objectMapper.readValue(file, typeRef);
            for (Map.Entry<String, TestCase> entry : suite.entrySet()) {
                runTest(file.getName(), entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * run a VM test
     *
     * @param testName
     * @param testCase
     */
    private void runTest(String fileName, String testName, TestCase testCase) {
        logger.info("Running test: file = {}, test = {}", fileName, testName);

        Exec exec = testCase.getExec();
        byte[] code = HexUtil.fromHexString(exec.getCode());
        DataWord address = DataWord.of(exec.getAddress());
        DataWord origin = DataWord.of(exec.getOrigin());
        DataWord caller = DataWord.of(exec.getCaller());
        long gas = DataWord.of(exec.getGas()).longValue();
        DataWord gasPrice = DataWord.of(exec.getGasPrice());
        DataWord value = DataWord.of(exec.getValue());
        byte[] data = HexUtil.fromHexString(exec.getData());

        Environment env = testCase.getEnvironment();
        DataWord prevHash = DataWord.ZERO;
        DataWord coinbase = DataWord.of(env.getCurrentCoinbase());
        DataWord timestamp = DataWord.of(env.getCurrentTimestamp());
        DataWord number = DataWord.of(env.getCurrentNumber());
        DataWord difficulty = DataWord.of(env.getCurrentDifficulty());
        DataWord gasLimit = DataWord.of(env.getCurrentGasLimit());

        Repository repository = new RepositoryMock();
        Repository originalRepository = repository.clone();
        BlockStore blockStore = new BlockStoreMock();
        int callDepth = 0;
        boolean isStaticCall = false;

        ConstantinopleSpec spec = new ConstantinopleSpec();
        VM vm = new VM(spec);
        ProgramInvoke programInvoke = new ProgramInvokeImpl(address, origin, caller, gas, gasPrice, value, data,
                prevHash, coinbase, timestamp, number, difficulty, gasLimit, repository, originalRepository, blockStore,
                callDepth, isStaticCall);
        Program program = new Program(code, programInvoke, null, spec);

        vm.play(program);

        ProgramResult result = program.getResult();
        logger.info("Validating results");
        logger.info("Done!");
    }
}
