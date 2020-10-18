package com.wbs.ldp.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSortedMap;

public abstract class _ClientMessage extends BaseMessage {
    protected _ClientMessage(ImmutableSortedMap<String, TypeReference<?>> serializableFields) {
        super(serializableFields);
    }
}
