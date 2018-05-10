package com.haoge.usefulcodes.tools;

import com.haoge.usefulcodes.utils.tools.CommonUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonUtilTest {

    @Test
    public void isEmpty() {
        assertTrue(CommonUtil.INSTANCE.isEmpty(null));
        assertTrue(CommonUtil.INSTANCE.isEmpty(""));
        assertFalse(CommonUtil.INSTANCE.isEmpty("Hello World"));
        assertTrue(CommonUtil.INSTANCE.isEmpty(new ArrayList()));
        assertTrue(CommonUtil.INSTANCE.isEmpty(new HashMap()));
        assertTrue(CommonUtil.INSTANCE.isEmpty(new Object[0]));
        assertFalse(CommonUtil.INSTANCE.isEmpty(new Object[2]));
    }
}