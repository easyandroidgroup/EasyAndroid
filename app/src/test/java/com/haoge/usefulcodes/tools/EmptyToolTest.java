package com.haoge.usefulcodes.tools;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class EmptyToolTest {

    @Test
    public void isEmpty() {
        assertTrue(EmptyTool.isEmpty(null));
        assertTrue(EmptyTool.isEmpty(""));
        assertFalse(EmptyTool.isEmpty("Hello World"));
        assertTrue(EmptyTool.isEmpty(new ArrayList()));
        assertTrue(EmptyTool.isEmpty(new HashMap()));
        assertTrue(EmptyTool.isEmpty(new Object[0]));
        assertFalse(EmptyTool.isEmpty(new Object[2]));
    }
}