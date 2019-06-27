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
package org.ethereum.vm.compliance.spec;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestCase {

    private Info info;
    private List<Object> callcreates = null;
    private Environment environment;
    private Exec exec;
    private String gas;
    private String logs;
    private String out;
    private Map<String, Account> post;
    private Map<String, Account> pre;

    @JsonProperty("_info")
    public Info getInfo() {
        return info;
    }

    @JsonProperty("_info")
    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Object> getCallcreates() {
        return callcreates;
    }

    public void setCallcreates(List<Object> callcreates) {
        this.callcreates = callcreates;
    }

    @JsonProperty("env")
    public Environment getEnvironment() {
        return environment;
    }

    @JsonProperty("env")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Exec getExec() {
        return exec;
    }

    public void setExec(Exec exec) {
        this.exec = exec;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public Map<String, Account> getPost() {
        return post;
    }

    public void setPost(Map<String, Account> post) {
        this.post = post;
    }

    public Map<String, Account> getPre() {
        return pre;
    }

    public void setPre(Map<String, Account> pre) {
        this.pre = pre;
    }
}
