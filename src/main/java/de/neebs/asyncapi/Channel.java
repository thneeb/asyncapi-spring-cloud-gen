package de.neebs.asyncapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Channel {
    private ChannelItem subscribe;
    private ChannelItem publish;
}
