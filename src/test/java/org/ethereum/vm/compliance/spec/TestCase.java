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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "_info",
        "callcreates",
        "environment",
        "exec",
        "gas",
        "logs",
        "out",
        "post",
        "pre"
})
public class TestCase {

    @JsonProperty("_info")
    private Info info;
    @JsonProperty("callcreates")
    private List<Object> callcreates = null;
    @JsonProperty("env")
    private Environment environment;
    @JsonProperty("exec")
    private Exec exec;
    @JsonProperty("gas")
    private String gas;
    @JsonProperty("logs")
    private String logs;
    @JsonProperty("out")
    private String out;
    @JsonProperty("post")
    private Map<String, Address> post;
    @JsonProperty("pre")
    private Map<String, Address> pre;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("_info")
    public Info getInfo() {
        return info;
    }

    @JsonProperty("_info")
    public void setInfo(Info info) {
        this.info = info;
    }

    @JsonProperty("callcreates")
    public List<Object> getCallcreates() {
        return callcreates;
    }

    @JsonProperty("callcreates")
    public void setCallcreates(List<Object> callcreates) {
        this.callcreates = callcreates;
    }

    @JsonProperty("environment")
    public Environment getEnvironment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @JsonProperty("exec")
    public Exec getExec() {
        return exec;
    }

    @JsonProperty("exec")
    public void setExec(Exec exec) {
        this.exec = exec;
    }

    @JsonProperty("gas")
    public String getGas() {
        return gas;
    }

    @JsonProperty("gas")
    public void setGas(String gas) {
        this.gas = gas;
    }

    @JsonProperty("logs")
    public String getLogs() {
        return logs;
    }

    @JsonProperty("logs")
    public void setLogs(String logs) {
        this.logs = logs;
    }

    @JsonProperty("out")
    public String getOut() {
        return out;
    }

    @JsonProperty("out")
    public void setOut(String out) {
        this.out = out;
    }

    @JsonProperty("post")
    public Map<String, Address> getPost() {
        return post;
    }

    @JsonProperty("post")
    public void setPost(Map<String, Address> post) {
        this.post = post;
    }

    @JsonProperty("pre")
    public Map<String, Address> getPre() {
        return pre;
    }

    @JsonProperty("pre")
    public void setPre(Map<String, Address> pre) {
        this.pre = pre;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
