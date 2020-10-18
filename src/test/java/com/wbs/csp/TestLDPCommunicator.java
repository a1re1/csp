package com.wbs.csp;

import com.wbs.csp.messages.BaseMessage;
import com.wbs.csp.protocol.LDPCommunicator;

import java.net.DatagramPacket;

public class TestLDPCommunicator implements LDPCommunicator {
    @Override
    public short[] getAckedMessageIds() {
        return new short[4];
    }

    @Override
    public void addToSentAckList(short identifier, BaseMessage message) {

    }

    @Override
    public void addToReceiveAckList(short identifier) {

    }

    @Override
    public void addToReceivedQueue(DatagramPacket packet, BaseMessage message) {

    }

    @Override
    public short nextId() {
        return 123;
    }
}
