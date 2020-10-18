package com.wbs.csp.messages;

import com.wbs.csp.protocol.LDPCommunicator;
import com.wbs.csp.TestLDPCommunicator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseMessageTest<MESSAGE extends BaseMessage> {

    private LDPCommunicator communicator;

    @Before
    public void setup() {
        communicator = new TestLDPCommunicator();
    }

    public abstract MESSAGE getTestMessage();

    @Test
    public void itSerializesToBytesThenDeserializesToEqualEntity() throws IOException {
        var testMessage = getTestMessage();
        byte[] bytes = testMessage.toByteArray(communicator::getAckedMessageIds).get();
        MESSAGE newMessage = (MESSAGE) BaseMessage.fromByteArray(bytes);

        assertThat(newMessage.getType()).isEqualTo(testMessage.getType());
        assertThat(newMessage.timestamp).isEqualTo(testMessage.timestamp);
        assertThat(newMessage.acks).isEqualTo(testMessage.acks);
        assertThat(newMessage.size).isEqualTo(testMessage.size);

        assertUniqueEntityPropertiesAreEqual(testMessage, newMessage);
    }

    private void assertUniqueEntityPropertiesAreEqual(MESSAGE first, MESSAGE second) {
        assertThat(first.serializableFieldsMap).hasSize(first.fieldTypesMap.size());
        assertThat(first.serializableFieldsMap).hasSize(second.serializableFieldsMap.size());
        assertThat(first.fieldTypesMap).hasSize(second.fieldTypesMap.size());

        first.serializableFieldsMap.keySet().forEach(field -> {
            assertThat(first.fieldTypesMap).containsKey(field);
            assertThat(second.fieldTypesMap).containsKey(field);
            assertThat(second.serializableFieldsMap).containsKey(field);
            assertThat(second.fieldTypesMap.get(field)).isEqualTo(first.fieldTypesMap.get(field));
            assertThat(second.serializableFieldsMap.get(field)).isEqualTo(first.serializableFieldsMap.get(field));
        });

    }
}
