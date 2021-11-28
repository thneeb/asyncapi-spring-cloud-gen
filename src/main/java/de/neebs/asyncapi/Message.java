package de.neebs.asyncapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private Definition headers;
    private Definition payload;
    private Bindings bindings;
}
