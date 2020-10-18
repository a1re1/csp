package com.wbs.csp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LDPCommunicatorTest.class,
        MessageTest.class
})
public class NetworkingTestSuite {
}
