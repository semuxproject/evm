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
