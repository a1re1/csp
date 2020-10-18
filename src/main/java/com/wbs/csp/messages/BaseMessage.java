package com.wbs.csp.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import com.wbs.csp.protocol.ByteArraySerializable;
import com.wbs.csp.util.BSH;
import com.wbs.simplog.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

@Accessors(chain = true)
public abstract class BaseMessage implements ByteArraySerializable {
    private static final Log LOG = new Log(BaseMessage.class);
    private static final int MAX_BYTE = 256;

    @VisibleForTesting final Map<String, Object> serializableFieldsMap;
    @VisibleForTesting final SortedMap<String, TypeReference<?>> fieldTypesMap;

    @VisibleForTesting @Getter @Setter short identifier;
    @VisibleForTesting @Getter long timestamp;
    @VisibleForTesting @Getter short[] acks;
    @VisibleForTesting short size = 0;

    protected BaseMessage(ImmutableSortedMap<String, TypeReference<?>> serializableFields) {
        this.timestamp = System.currentTimeMillis();
        fieldTypesMap = serializableFields;
        serializableFieldsMap = new HashMap<>(serializableFields.size());
        serializableFields.forEach((field, fieldType) -> serializableFieldsMap.put(field, null));
    }

    protected <TYPE> TYPE get(String name) {
        return (TYPE) serializableFieldsMap.get(name);
    }

    protected <TYPE> void set(String name, TYPE data) {
        if (!serializableFieldsMap.containsKey(name)) return;
        serializableFieldsMap.put(name, data);
    }

    public abstract byte getType();

    private byte[] getBody() throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (var field : fieldTypesMap.entrySet()) {
            BSH.write(body, field.getValue(), get(field.getKey()));
        }
        return body.toByteArray();
    }

    private void fromBody(byte[] body) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(body);
        for (var field : fieldTypesMap.entrySet()) {
            Object data;
            data = BSH.read(bodyBuffer, field.getValue());
            set(field.getKey(), data);
        }
    }

    public Optional<byte[]> toByteArray(Supplier<short[]> getAcks) {
        try {
            return Optional.of(toByteArray(getAcks, false));
        } catch (IOException e) {
            LOG.error("Could not serialize message: {}", this, e);
            return Optional.empty();
        }
    }

    public byte[] toByteArray(Supplier<short[]> getAcks, boolean dryRun) throws IOException {
        acks = dryRun ? new short[4] : getAcks.get();
        byte[] body = getBody();

        size = (short) (Short.BYTES             // size
                + Short.BYTES                   // identifier
                + acks.length * Short.BYTES     // ack'ed packets
                + Long.BYTES                    // timestamp
                + 1                             // type (byte)
                + body.length);

        ByteBuffer encoded = ByteBuffer.allocate(size);
        encoded.putShort(size);
        encoded.putShort(identifier);
        for(short s : acks) {
            encoded.putShort(s);
        }

        encoded.putLong(timestamp);
        encoded.put(getType());
        encoded.put(body);

        Preconditions.checkArgument(size <= MAX_BYTE,
                "Packet len longer than max bytes -- size: " + size
                        + ", encoded: " + Arrays.toString(encoded.array()));

        return encoded.array();
    }

    public static BaseMessage fromByteArray(byte[] packetBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetBytes);
        int size = byteBuffer.getShort();
        short messageId = byteBuffer.getShort();

        short[] acks = new short[4];
        for (int i = 0; i < acks.length; i++) acks[i] = byteBuffer.getShort();

        long nanoTime = byteBuffer.getLong();
        BaseMessage message = MessageRegistry.createEmptyMessage(byteBuffer.get())
                .setIdentifier(messageId);

        message.timestamp = nanoTime;
        message.acks = acks;
        message.size = (short) size;

        message.fromBody(Arrays.copyOfRange(packetBytes, byteBuffer.position(), size));
        return message;
    }

    @Override
    public String toString() {
        try {
            return "\n|| [Message] ||\n" +
                    "||-----------||\n" +
                    "|| size: \t\t" + size + "\n" +
                    "|| identifier: \t" + identifier + "\n" +
                    "|| acks: \t\t" + Arrays.toString(acks) + "\n" +
                    "|| ts: \t\t\t" + timestamp + "\n" +
                    "|| type: \t\t" + getType() + "\n" +
                    "|| body: \t\t" + Arrays.toString(getBody()) + "\n";
        } catch (IOException e) {
            return "Failed to parse message.";
        }
    }

    protected static void register(byte type, Supplier<BaseMessage> creatEmptyMessage) {
        MessageRegistry.registerMessage(type, creatEmptyMessage);
    }
}
