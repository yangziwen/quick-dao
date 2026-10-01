package io.github.yangziwen.quickdao.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilTest {

    @Test
    public void testGetSuperClassGenericTypes() {
        Type[] types = ReflectionUtil.getSuperClassGenericTypes(StringListHolder.class);
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(String.class, types[0]);
    }

    @Test
    public void testGetSuperClassGenericType() {
        Assert.assertEquals(String.class, ReflectionUtil.getSuperClassGenericType(StringListHolder.class, 0));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSuperClassGenericTypeWithInvalidIndex() {
        ReflectionUtil.getSuperClassGenericType(StringListHolder.class, 5);
    }

    @Test
    public void testGetFieldValue() {
        Item item = new Item(42, "hello");
        Field field = getField("id");
        Assert.assertEquals(Integer.valueOf(42), ReflectionUtil.getFieldValue(item, field));
    }

    @Test
    public void testSetFieldValue() {
        Item item = new Item(0, "");
        Field field = getField("name");
        ReflectionUtil.setFieldValue(item, field, "world");
        Assert.assertEquals("world", item.getName());
    }

    private Field getField(String name) {
        try {
            return Item.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("serial")
    static abstract class StringListHolder extends java.util.ArrayList<String> {}

    public static class Item {
        private int id;
        private String name;
        public Item(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
    }

}
