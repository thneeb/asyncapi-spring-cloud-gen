package de.neebs.asyncapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelItem {
    private String summary;
    private Message message;
    @JsonProperty("$ref")
    private String reference;
}
