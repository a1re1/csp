package com.wbs.csp.messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MessageRegistry {
    private static final Map<Byte, Supplier<BaseMessage>> MESSAGE_REGISTRY = new ConcurrentHashMap<>();

    public static void registerMessage(byte typeId, Supplier<BaseMessage> createEmptyEntity) {
        if (MESSAGE_REGISTRY.containsKey(typeId)) {
            throw new RuntimeException("Duplicate entity registry for typeId: " + typeId);
        }

        MESSAGE_REGISTRY.put(typeId, createEmptyEntity);
    }

    static BaseMessage createEmptyMessage(byte typeId) {
        if (!MESSAGE_REGISTRY.containsKey(typeId)) {
            throw new RuntimeException("Unexpected message typeId: " + typeId);
        }

        return MESSAGE_REGISTRY.get(typeId).get();
    }
}
