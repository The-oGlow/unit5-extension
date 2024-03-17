package com.glowanet.util.junit.jupiter.api.extension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ErrorCollectorMultipleExceptionIT {

    @RegisterExtension
    public ErrorCollector collector = new ErrorCollector();

    @Disabled("Currently don't know how to test this")
    @Test
    public void testWithException() {
        //FIXME:Currently don't know how to test this
        collector.checkThat(20, equalTo(1));
        collector.checkThat(30, equalTo(1));
    }

    @AfterEach
    public void tearDown() {
        assertThat(collector.getErrorSize(), equalTo(2));
    }

}