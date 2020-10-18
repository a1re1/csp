package com.wbs.ldp.data;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.UUID;

public class ServerConnection {
    public final InetAddress addr;
    public final int port;
    public final UUID playerId;

    public short mainEntityId; // fixme this probably is dependent on the scene

    public ServerConnection(InetAddress address, int port, @Nullable UUID playerId) {
        this.addr = address;
        this.port = port;
        this.playerId = playerId;
    }

    public void setMainEntity(short id) {
        mainEntityId = id;
    }

    public void close() {

    }

    @Override
    public String toString() {
        return "{Connection: " + addr.toString() + ":" + port + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ServerConnection
                && ((ServerConnection) obj).addr.toString().equals(this.addr.toString())
                && ((ServerConnection) obj).port == this.port;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
