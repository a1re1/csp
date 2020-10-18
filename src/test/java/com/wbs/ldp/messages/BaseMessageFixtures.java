package com.wbs.ldp.messages;

import java.util.function.Supplier;

public interface BaseMessageFixtures {
    Supplier<TestServerMessage> TEST_SERVER_MESSAGE_SUPPLIER = TestServerMessage::new;
}
