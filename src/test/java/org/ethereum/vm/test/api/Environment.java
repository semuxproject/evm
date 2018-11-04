
package org.ethereum.vm.test.api;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "currentCoinbase",
        "currentDifficulty",
        "currentGasLimit",
        "currentNumber",
        "currentTimestamp"
})
public class Environment {

    @JsonProperty("currentCoinbase")
    private String currentCoinbase;
    @JsonProperty("currentDifficulty")
    private String currentDifficulty;
    @JsonProperty("currentGasLimit")
    private String currentGasLimit;
    @JsonProperty("currentNumber")
    private String currentNumber;
    @JsonProperty("currentTimestamp")
    private String currentTimestamp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("currentCoinbase")
    public String getCurrentCoinbase() {
        return currentCoinbase;
    }

    @JsonProperty("currentCoinbase")
    public void setCurrentCoinbase(String currentCoinbase) {
        this.currentCoinbase = currentCoinbase;
    }

    @JsonProperty("currentDifficulty")
    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    @JsonProperty("currentDifficulty")
    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    @JsonProperty("currentGasLimit")
    public String getCurrentGasLimit() {
        return currentGasLimit;
    }

    @JsonProperty("currentGasLimit")
    public void setCurrentGasLimit(String currentGasLimit) {
        this.currentGasLimit = currentGasLimit;
    }

    @JsonProperty("currentNumber")
    public String getCurrentNumber() {
        return currentNumber;
    }

    @JsonProperty("currentNumber")
    public void setCurrentNumber(String currentNumber) {
        this.currentNumber = currentNumber;
    }

    @JsonProperty("currentTimestamp")
    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    @JsonProperty("currentTimestamp")
    public void setCurrentTimestamp(String currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
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
