
package org.ethereum.vm.test.api;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @JsonProperty("environment")
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
