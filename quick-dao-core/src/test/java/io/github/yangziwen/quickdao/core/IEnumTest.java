package io.github.yangziwen.quickdao.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class IEnumTest {

    @Test
    public void testExtractEnumValueWithNull() {
        Assert.assertNull(IEnum.extractEnumValue(null));
    }

    @Test
    public void testExtractEnumValueWithSingleIEnum() {
        Assert.assertEquals(1, IEnum.extractEnumValue(Color.RED));
    }

    @Test
    public void testExtractEnumValueWithEmptyCollection() {
        Collection<?> empty = Collections.emptyList();
        Assert.assertSame(empty, IEnum.extractEnumValue(empty));
    }

    @Test
    public void testExtractEnumValueWithIEnumCollection() {
        Object result = IEnum.extractEnumValue(Arrays.asList(Color.RED, Color.GREEN));
        Assert.assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(2, list.get(1));
    }

    @Test
    public void testExtractEnumValueWithIEnumArray() {
        Object result = IEnum.extractEnumValue(new Color[] { Color.RED, Color.GREEN });
        Assert.assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals(1, arr[0]);
        Assert.assertEquals(2, arr[1]);
    }

    @Test
    public void testExtractEnumValueWithNonIEnum() {
        Assert.assertNull(IEnum.extractEnumValue("not an enum"));
        Assert.assertNull(IEnum.extractEnumValue(42));
    }

    @Test
    public void testExtractEnumValueWithNonIEnumCollection() {
        Assert.assertNull(IEnum.extractEnumValue(Arrays.asList("a", "b")));
    }

    enum Color implements IEnum<Color, Integer> {
        RED(1), GREEN(2), BLUE(3);
        private Integer v;
        Color(Integer v) { this.v = v; }
        @Override
        public Integer getValue() { return v; }
    }

}
