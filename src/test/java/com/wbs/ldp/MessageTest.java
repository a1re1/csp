package com.wbs.ldp;

import com.wbs.ldp.messages.BaseMessageFixtures;
import com.wbs.ldp.messages.BaseMessageTest;
import com.wbs.ldp.messages.TestServerMessage;

public class MessageTest extends BaseMessageTest<TestServerMessage> {
    @Override
    public TestServerMessage getTestMessage() {
        return BaseMessageFixtures.TEST_SERVER_MESSAGE_SUPPLIER.get();
    }
}
