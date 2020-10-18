package com.wbs.ldp.messages;

import java.net.DatagramPacket;

public interface Handler {
    void handle(DatagramPacket packet, BaseMessage message) throws Exception;
}
