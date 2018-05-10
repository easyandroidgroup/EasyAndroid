package com.haoge.usefulcodes.tools;

import com.haoge.usefulcodes.utils.tools.CommonTool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonToolTest {

    @Test
    public void isEmpty() {
        assertTrue(CommonTool.INSTANCE.isEmpty(null));
        assertTrue(CommonTool.INSTANCE.isEmpty(""));
        assertFalse(CommonTool.INSTANCE.isEmpty("Hello World"));
        assertTrue(CommonTool.INSTANCE.isEmpty(new ArrayList()));
        assertTrue(CommonTool.INSTANCE.isEmpty(new HashMap()));
        assertTrue(CommonTool.INSTANCE.isEmpty(new Object[0]));
        assertFalse(CommonTool.INSTANCE.isEmpty(new Object[2]));
    }
}