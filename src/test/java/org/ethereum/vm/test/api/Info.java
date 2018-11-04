
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
        "comment",
        "filledwith",
        "lllcversion",
        "source",
        "sourceHash"
})
public class Info {

    @JsonProperty("comment")
    private String comment;
    @JsonProperty("filledwith")
    private String filledwith;
    @JsonProperty("lllcversion")
    private String lllcversion;
    @JsonProperty("source")
    private String source;
    @JsonProperty("sourceHash")
    private String sourceHash;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("filledwith")
    public String getFilledwith() {
        return filledwith;
    }

    @JsonProperty("filledwith")
    public void setFilledwith(String filledwith) {
        this.filledwith = filledwith;
    }

    @JsonProperty("lllcversion")
    public String getLllcversion() {
        return lllcversion;
    }

    @JsonProperty("lllcversion")
    public void setLllcversion(String lllcversion) {
        this.lllcversion = lllcversion;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("sourceHash")
    public String getSourceHash() {
        return sourceHash;
    }

    @JsonProperty("sourceHash")
    public void setSourceHash(String sourceHash) {
        this.sourceHash = sourceHash;
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
