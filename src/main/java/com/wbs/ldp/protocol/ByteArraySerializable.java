package com.wbs.ldp.protocol;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public interface ByteArraySerializable {
    Optional<byte[]> toByteArray(Supplier<short[]> getAcks) throws IOException;
}
