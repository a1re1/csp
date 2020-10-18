package com.wbs.csp.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

// Buffer Serialization Helper
public abstract class BSH {

    ///////////////////
    // Type Handlers //
    ///////////////////
    private static final ConcurrentHashMap<Type, TypeHandler<?>> handlers = new ConcurrentHashMap<>();

    public static <TYPE> void registerTypeHandler(TypeReference<TYPE> typeReference,
                                                  BiFunction<ByteArrayOutputStream, TYPE, ByteArrayOutputStream> writeToBuffer,
                                                  Function<ByteBuffer, TYPE> readFromBuffer) {
        handlers.put(typeReference.getType(), new TypeHandler<>(writeToBuffer, readFromBuffer));
    }

    //
    // Register basic types
    //
    static {
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, integer) -> BSH.write(buffer, ByteBuffer.allocate(Integer.BYTES).putInt(integer).array()),
                ByteBuffer::getInt);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, shrt) -> BSH.write(buffer, ByteBuffer.allocate(Short.BYTES).putShort(shrt).array()),
                ByteBuffer::getShort);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, lng) -> BSH.write(buffer, ByteBuffer.allocate(Long.BYTES).putLong(lng).array()),
                ByteBuffer::getLong);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, flot) -> BSH.write(buffer, ByteBuffer.allocate(Float.BYTES).putFloat(flot).array()),
                ByteBuffer::getFloat);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, doble) -> BSH.write(buffer, ByteBuffer.allocate(Double.BYTES).putDouble(doble).array()),
                ByteBuffer::getDouble);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, chr) -> BSH.write(buffer, ByteBuffer.allocate(Character.BYTES).putChar(chr).array()),
                ByteBuffer::getChar);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, bool) -> BSH.write(buffer, ByteBuffer.allocate(1).put(bool ? (byte) 1 : (byte) 0).array()),
                (buffer) -> buffer.get() == (byte) 1);
        registerTypeHandler(
                new TypeReference<>() {},
                BSH::writeString,
                BSH::readString);
        registerTypeHandler(
                new TypeReference<>() {},
                BSH::writeBytes,
                BSH::readBytes);
        registerTypeHandler(
                new TypeReference<>() {},
                (buffer, uuid) -> BSH.write(buffer, writeUUID(uuid)),
                BSH::readUUID);
    }

    /////////////
    // Setters //
    /////////////
    public static<DATATYPE> void writeCollection(ByteBuffer buffer, Collection<DATATYPE> collection, BiFunction<ByteBuffer, DATATYPE, Void> writeData) {
        for (DATATYPE data : collection){
            writeData.apply(buffer,data);
        }
    }

    public static <DATATYPE> void writeArray(ByteBuffer buffer, DATATYPE[] array, BiFunction<ByteBuffer, DATATYPE, ByteBuffer> writeData) {
        buffer.putInt(array.length);
        for (DATATYPE data : array){
            writeData.apply(buffer,data);
        }
    }

    public static ByteArrayOutputStream writeString(ByteArrayOutputStream buffer, Object data) {
        String out = (String) data;
        byte[] str = out.getBytes(StandardCharsets.UTF_8);
        BSH.write(buffer, ByteBuffer.allocate(Integer.BYTES).putInt(str.length).array());
        BSH.write(buffer, str);
        return buffer;
    }

    public static ByteArrayOutputStream writeBytes(ByteArrayOutputStream buffer, Object data) {
        byte[] bytes = (byte[]) data;
        BSH.write(buffer, ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        BSH.write(buffer,bytes);
        return buffer;
    }

    public static <TYPE> ByteArrayOutputStream write(ByteArrayOutputStream buffer, TypeReference<TYPE> type, TYPE data) throws IOException {
        Optional<?> handlerMaybe = Optional.ofNullable(handlers.get(type.getType()));

        if (handlerMaybe.isEmpty()) {
            throw new RuntimeException("Unexpected type: " + type.getType());
        }

        ((TypeHandler<TYPE>) handlerMaybe.get()).write
                .apply(buffer, data);

        return buffer;
    }

    public static byte[] writeUUID(UUID uuid) {
        ByteBuffer uuidBuffer = ByteBuffer.allocate(2 * Long.BYTES);
        uuidBuffer.putLong(uuid.getMostSignificantBits());
        uuidBuffer.putLong(uuid.getLeastSignificantBits());
        return uuidBuffer.array();
    }

    /////////////
    // Getters //
    /////////////

    public static <TYPE> TYPE read(ByteBuffer buffer, TypeReference<TYPE> type) {
        Optional<?> handlerMaybe = Optional.ofNullable(handlers.get(type.getType()));

        if (handlerMaybe.isEmpty()) {
            throw new RuntimeException("Unexpected type: " + type.getType());
        }

        return ((TypeHandler<TYPE>) handlerMaybe.get()).read
                .apply(buffer);
    }

    public static byte[] readBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] dest = new byte[length];
        buffer.get(dest);
        return dest;
    }

    public static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        return readUtf8String(buffer, length);
    }

    public static String readUtf8String(ByteBuffer buffer, int length) {
        byte[] nameBuffer = new byte[length];
        buffer.get(nameBuffer, 0, length);
        return new String(nameBuffer, StandardCharsets.UTF_8);
    }

    public static UUID readUUID(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static <DATATYPE> ArrayList<DATATYPE> readArray(ByteBuffer buffer, Function<ByteBuffer, DATATYPE> readData) {
        int arraySize = buffer.getInt();
        ArrayList<DATATYPE> outputArray = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++){
            outputArray.add(readData.apply(buffer));
        }
        return outputArray;
    }

    private static ByteArrayOutputStream write(ByteArrayOutputStream writeBuffer, byte[] bytes) {
        try {
            writeBuffer.write(bytes);
            return writeBuffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TypeHandler<TYPE> {
        BiFunction<ByteArrayOutputStream, TYPE, ByteArrayOutputStream> write;
        Function<ByteBuffer, TYPE> read;

        private TypeHandler(BiFunction<ByteArrayOutputStream, TYPE, ByteArrayOutputStream> writeToBuffer,
                            Function<ByteBuffer, TYPE> readFromBuffer) {
            this.write = writeToBuffer;
            this.read = readFromBuffer;
        }
    }
}
