package com.wbs.ldp.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSortedMap;

public abstract class _ServerMessage extends BaseMessage {
    protected _ServerMessage(ImmutableSortedMap<String, TypeReference<?>> serializableFields) {
        super(serializableFields);
    }
}
