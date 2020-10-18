package com.wbs.csp.server;

import com.wbs.java.utils.Pair;
import com.wbs.java.utils.config.Config;
import com.wbs.java.utils.config.ConfigReader;
import com.wbs.csp.messages.BaseMessage;
import com.wbs.csp.messages.ServerMessageHandler;
import com.wbs.csp.messages._ServerMessage;
import com.wbs.csp.protocol.LDPCommunicator;
import com.wbs.csp.protocol.ReceiveQueue;
import com.wbs.csp.data.ServerConnection;
import com.wbs.simplog.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class LDPServer implements LDPCommunicator {
    private static final Log LOG = new Log(LDPServer.class);
    
    private static final short EMPTY = 0;

    private volatile ConcurrentLinkedQueue<Pair<DatagramPacket, BaseMessage>> receivedMessages = new ConcurrentLinkedQueue<>();
    private volatile ConcurrentLinkedQueue<Short> ackedMessages = new ConcurrentLinkedQueue<>();
    private volatile Map<Short, BaseMessage> sendAckMap = new ConcurrentHashMap<>();
    private volatile Set<Short> receiveAckMap = new ConcurrentSkipListSet<>();

    protected final HashMap<UUID, ServerConnection> connections = new HashMap<>();

    protected final ServerMessageHandler handler = new ServerMessageHandler();

    private ReceiveQueue receiveQueue;
    private DatagramSocket socket;
    private int port;

    private boolean running;
    private short serverId = 0;

    protected LDPServer() throws SocketException {
        var config = loadConfig();

        openUdpSocket(config);
        startReceiveQueue();
        registerReceivableMessageHandlers();

        running = true;
    }

    ////////////////////////
    /// Abstract Methods ///
    ////////////////////////

    protected abstract void registerReceivableMessageHandlers();

    protected abstract void close();

    protected abstract void update();

    //////////////////////
    /// Public Methods ///
    //////////////////////

    public void addToReceivedQueue(DatagramPacket packet, BaseMessage message) {
        receivedMessages.add(new Pair<>(packet, message));
        LOG.trace("added message to receive queue: {}", receivedMessages);
        addToReceiveAckList(message.getIdentifier());
    }

    // TODO if I want some sort of whenClientSeen() function it goes here
    // todo need to change this to acked in context of a certain client
    @Override
    public short[] getAckedMessageIds() {
        return new short[] {
                Optional.ofNullable(ackedMessages.poll()).orElse(EMPTY),
                Optional.ofNullable(ackedMessages.poll()).orElse(EMPTY),
                Optional.ofNullable(ackedMessages.poll()).orElse(EMPTY),
                Optional.ofNullable(ackedMessages.poll()).orElse(EMPTY)
        };
    }

    @Override
    public void addToSentAckList(short identifier, BaseMessage message) {
        sendAckMap.put(identifier, message);
    }

    @Override
    public void addToReceiveAckList(short identifier) {
        receiveAckMap.add(identifier);
        ackedMessages.add(identifier);
    }

    @Override
    public short nextId() {
        if (serverId == Short.MIN_VALUE) serverId = 0;
        serverId--;
        return serverId;
    }

    /////////////////////////
    /// Protected Methods ///
    /////////////////////////

    protected void start() {
        while(running) {
            handleReceivedMessages();
            update();
        }
    }

    protected void stop() {
        running = false;
        receiveQueue.close();
        close();
    }

    protected void broadcastMessage(_ServerMessage message) {
        var buffer = convertMessageToBytes(message);
        buffer.ifPresent(buf -> connections.forEach((key, c) -> sendPacket(c, buf)));
    }

    protected void sendMessage(ServerConnection connection, _ServerMessage message) {
        var buffer = convertMessageToBytes(message);
        buffer.ifPresent(buf -> sendPacket(connection, buf));
    }

    ///////////////////////
    /// Private Methods ///
    ///////////////////////

    private void openUdpSocket(Config config) throws SocketException {
        port = config.getAsIntOrDefault("PORT", 25966);
        socket = new DatagramSocket(port);
        LOG.info("Started server... Listening on port: {}", port);
    }

    private void sendPacket(ServerConnection connection, byte[] buffer) {
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length, connection.addr, connection.port);
        try {
            socket.send(pkt);
        } catch (IOException e) {
            connection.close();
            LOG.error("[addr:{}-port:{}] Could not send packet. ", connection.addr, connection.port, e);
        }
    }

    private Optional<byte[]> convertMessageToBytes(_ServerMessage message) {
        message.setIdentifier(nextId());
        return message.toByteArray(this::getAckedMessageIds);
    }

    private Config loadConfig() {
        Config config = ConfigReader.readConfig("./saves/", "server.config");
        Log.setLevel(config.getAsStringOrDefault("LOGLEVEL", "INFO"));
        return config;
    }

    private void startReceiveQueue() {
        receiveQueue = new ReceiveQueue(this, socket);
        receiveQueue.start();
    }

    private void handleReceivedMessages() {
        if (!receivedMessages.isEmpty()) {
            for (int i = 0; i < receivedMessages.size(); i++) {
                var pair = receivedMessages.poll();
                handler.handle(pair.first, pair.second);
            }
        }
    }

}
