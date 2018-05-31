package com.haoge.easyandroid.easy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * @author haoge on 2018/5/31
 */
@RunWith(JUnit4.class)
public class EasyReflectTest {

    @Test
    public void box() {
        assertEquals(Integer.class, EasyReflect.box(int.class));
        assertEquals(Boolean.class, EasyReflect.box(boolean.class));
        assertEquals(Byte.class, EasyReflect.box(byte.class));
        assertEquals(Character.class, EasyReflect.box(char.class));
        assertEquals(Short.class, EasyReflect.box(short.class));
        assertEquals(Long.class, EasyReflect.box(long.class));
        assertEquals(Float.class, EasyReflect.box(float.class));
        assertEquals(Double.class, EasyReflect.box(double.class));
        assertEquals(String.class, EasyReflect.box(String.class));
    }

    @Test
    public void match() {
        Class<?>[] declared = {Integer.class, String.class, CharSequence.class, int.class};
        Class<?>[] actual = {int.class, String.class, StringBuilder.class, Integer.class};
        assertTrue(EasyReflect.match(declared, actual));
    }
}