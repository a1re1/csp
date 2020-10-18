package com.wbs.ldp.data;

import com.wbs.java.utils.immutables.WbsStyle;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@WbsStyle
public interface UserIF {
    @Value.Parameter
    UUID getUserId();
}
