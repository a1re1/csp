package com.wbs.csp;

import com.wbs.csp.messages.BaseMessageFixtures;
import com.wbs.csp.messages.BaseMessageTest;
import com.wbs.csp.messages.TestServerMessage;

public class MessageTest extends BaseMessageTest<TestServerMessage> {
    @Override
    public TestServerMessage getTestMessage() {
        return BaseMessageFixtures.TEST_SERVER_MESSAGE_SUPPLIER.get();
    }
}
