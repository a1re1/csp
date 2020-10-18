package com.wbs.ldp.protocol;

import com.wbs.ldp.messages.BaseMessage;
import com.wbs.simplog.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiveQueue extends Thread {
    private static final Log LOG = new Log(ReceiveQueue.class);
    public static final int BUFFER_LENGTH = 256;

    private final LDPCommunicator serverExecutable;
    private final DatagramSocket socket;
    private final byte[] receiveBuffer = new byte[BUFFER_LENGTH];

    private boolean running;

    public ReceiveQueue(LDPCommunicator serverExecutable, DatagramSocket socket) {
        this.socket = socket;
        this.serverExecutable = serverExecutable;
        this.running = true;
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        running = true;

        try {
            while (running) {
                LOG.trace("Waiting for next message.");
                DatagramPacket packet
                        = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(packet);

                BaseMessage message = BaseMessage.fromByteArray(packet.getData());
                LOG.trace("Found message: {}", () -> message);
                serverExecutable.addToReceivedQueue(packet, message);
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        socket.close();
    }

    public void close() {
        if (socket != null && running) {
            running = false;
        }
    }

}
