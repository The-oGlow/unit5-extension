package com.glowanet.util.junit.jupiter.api.extension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ErrorCollectorOneExceptionIT {

    @RegisterExtension
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void testWithException() {
        collector.checkThat(20, equalTo(1));
    }

    @AfterEach
    public void tearDown() {
        assertThat(collector.getErrorSize(), equalTo(1));
    }

}