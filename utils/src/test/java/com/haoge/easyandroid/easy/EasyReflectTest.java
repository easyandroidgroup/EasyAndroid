package com.haoge.easyandroid.easy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author haoge on 2018/5/31
 */
@RunWith(JUnit4.class)
public class EasyReflectTest {

    EasyReflect reflect;

    @Before
    public void setUp() {
        reflect = EasyReflect.create(TestReflectClass.class);
    }

    @Test
    public void create() {
        // 只使用class进行创建时。只有clazz数据，没有instance数据
        EasyReflect reflect = EasyReflect.create(TestReflectClass.class);
        assertEquals(TestReflectClass.class, reflect.getClazz());
        assertNull(reflect.getInstance());

        // 使用具体类实例创建，具备clazz与instance实例
        TestReflectClass instance = new TestReflectClass();
        reflect = EasyReflect.create(instance);
        assertEquals(TestReflectClass.class, reflect.getClazz());
        assertEquals(instance, reflect.getInstance());

        // 使用指定的类全名进行创建
        reflect = EasyReflect.create(TestReflectClass.class.getCanonicalName());
        assertEquals(TestReflectClass.class, reflect.getClazz());
    }

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
    public void types() {
        Class<?>[] types = EasyReflect.types(1, "2", 5f, new StringBuilder("New"), null);// null将被解析为Void
        assertArrayEquals(new Class[]{Integer.class, String.class, Float.class, StringBuilder.class, Void.class}, types);
    }

    @Test
    public void match() {
        Class<?>[] declared = {Integer.class, String.class, CharSequence.class, int.class};
        Class<?>[] actual = {int.class, String.class, StringBuilder.class, Integer.class};
        Class<?>[] actualWithVoid = {Integer.class, Void.class, Void.class, int.class};
        Class<?>[] actualWithVoid2 = {Integer.class, Void.class, String.class, Void.class};
        assertTrue(EasyReflect.match(declared, actual));
        assertTrue(EasyReflect.match(declared, actualWithVoid));
        assertFalse(EasyReflect.match(declared, actualWithVoid2));
    }

    @Test
    public void createWithConstructors() {
        // 1. 使用可变参数构造器进行创建：可变参数实际是数组
        assertArrayEquals(((String[]) ((TestReflectClass) reflect.instance(new Object[]{new String[]{"Hello", "World"}}).get()).value), new String[]{"Hello", "World"});
        // 2. 使用指定参数进行创建
        assertEquals(((TestReflectClass) reflect.instance("Hello").get()).value, "Hello");
    }

    @Test
    public void callWithMethods() {
        // 1. 调用可变参数方法
        reflect.call("varargsParams", new Object[]{new String[]{"Hello", "World"}});
        // 2. 调用多类型参数
        reflect.call("multiple", 1, "Hello", new StringBuilder("World"));
        // 3. 调用方法并获取返回值
        assertEquals("Hello World", reflect.callWithReturn("getMessage", "Hello World").get());
        // 4. 执行无访问权限的静态方法
        reflect.call("staticInvoked");
        // 5. 访问多参数带可变参数
        reflect.call("multipleWithVarargs", "name", new String[]{});
    }

    @Test
    public void fields() {
        // 1. 访问成员变量
        assertEquals(reflect.getFieldValue("TAG"), TestReflectClass.class.getCanonicalName());
        assertEquals(reflect.getFieldValue("name"), "FieldName");
        assertEquals(reflect.getFieldValue("value"), null);
        // 2. 成员变量赋值
        reflect.setField("TAG", "NEW_TAG");
        reflect.setField("name", "new_name");
        assertEquals(reflect.getFieldValue("TAG"), "NEW_TAG");
        assertEquals(reflect.getFieldValue("name"), "new_name");
    }

}

class TestReflectClass {
    private final String TAG = TestReflectClass.class.getCanonicalName();
    private static String name = "FieldName";

    public Object value;
    TestReflectClass() {}
    TestReflectClass(String ... args) { this.value = args; }
    TestReflectClass(CharSequence message){ this.value = message; }

    public void varargsParams(String ... args) {}
    public void multipleWithVarargs(String first, String...varargs) {}
    public void multiple(int account, String name, CharSequence sequence){}
    public String getMessage(String message) {
        return message;
    }
    private static void staticInvoked() { }
}