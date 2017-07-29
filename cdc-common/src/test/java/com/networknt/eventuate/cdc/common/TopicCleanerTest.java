package com.networknt.eventuate.cdc.common;

import org.junit.Assert;
import org.junit.Test;

public class TopicCleanerTest {
    @Test
    public void testClean() {
        String s = "abc";
        Assert.assertTrue(s.equals(TopicCleaner.clean(s)));
        s = "$abc";
        Assert.assertTrue("_DLR_abc".equals(TopicCleaner.clean(s)));
    }
}
