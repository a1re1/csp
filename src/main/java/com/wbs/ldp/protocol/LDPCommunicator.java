package com.wbs.ldp.protocol;

import com.wbs.ldp.messages.BaseMessage;

import java.net.DatagramPacket;

/**
 * Lossless User Datagram Protocol
 */
// note: this needs to be an interface because ExternalServer cannot extends 2 classes
// :: this seems kind of brittle, might be worth a refactor in the future
public interface LDPCommunicator {
    short[] getAckedMessageIds();
    void addToSentAckList(short identifier, BaseMessage message);
    void addToReceiveAckList(short identifier);
    void addToReceivedQueue(DatagramPacket packet, BaseMessage message);
    short nextId();
}
