package com.wbs.ldp.messages;

import com.wbs.simplog.Log;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class _MessageHandler {
    private static final Log LOG = new Log(_MessageHandler.class);
    private final Map<Byte, Handler> handlerMap = new HashMap<>();

    public void bindHandler(byte type, Handler handler) {
        handlerMap.put(type, handler);
    }

    // todo dont know if i like this since local version will not have a packet // maybe I just ignore it though and null?
    public void handle(DatagramPacket packet, BaseMessage message) {
        try {
            LOG.trace("handling message: {}", message);
            Optional.ofNullable(handlerMap.get(message.getType()))
                    .orElse((ignored, alsoIgnored) -> LOG.warn("Message skipped as it has no handler: {}", message))
                    .handle(packet, message);
        } catch (Exception e) {
            LOG.error("Error handling message: {}", e);
        }
    }
}
