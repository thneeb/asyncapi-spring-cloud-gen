package de.neebs.asyncapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Definition {
    private String type;
    private String description;
    private Map<String, Definition> properties;
    private List<String> required;
    @JsonProperty("enum")
    private List<String> enumeration;
    private Discriminator discriminator;
    @JsonProperty("$ref")
    private String reference;
    private List<Map<String, Object>> allOf;
    private List<Definition> oneOf;
}
