package de.neebs.asyncapi;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Components {
    private Map<String, Definition> schemas;
    private Map<String, Message> messages;
    private Object securitySchemes;
}
