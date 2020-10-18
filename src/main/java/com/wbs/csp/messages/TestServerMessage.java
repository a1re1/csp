package com.wbs.csp.messages;

import com.google.common.collect.ImmutableSortedMap;

public class TestServerMessage extends _ServerMessage {
    private static final byte TYPE = (byte) 12;

    static {
        register(TYPE, TestServerMessage::new);
    }

    public TestServerMessage() {
        super(ImmutableSortedMap.of());
    }

    @Override
    public byte getType() {
        return TYPE;
    }
}
