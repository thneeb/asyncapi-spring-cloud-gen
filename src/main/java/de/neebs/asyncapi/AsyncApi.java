package de.neebs.asyncapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsyncApi {
    private String asyncapi;
    private Map<String, Object> info;
    private Map<String, Channel> channels;
    private Components components;
}
