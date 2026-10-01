package io.github.yangziwen.quickdao.core;

import org.junit.Assert;
import org.junit.Test;

public class OperatorTest {

    private static final String STMT = "name";
    private static final String PH = "?";

    @Test
    public void testEq() {
        Assert.assertEquals("name = ?", Operator.eq.buildCondition(STMT, PH));
    }

    @Test
    public void testNe() {
        Assert.assertEquals("name != ?", Operator.ne.buildCondition(STMT, PH));
    }

    @Test
    public void testGt() {
        Assert.assertEquals("name > ?", Operator.gt.buildCondition(STMT, PH));
    }

    @Test
    public void testGe() {
        Assert.assertEquals("name >= ?", Operator.ge.buildCondition(STMT, PH));
    }

    @Test
    public void testLt() {
        Assert.assertEquals("name < ?", Operator.lt.buildCondition(STMT, PH));
    }

    @Test
    public void testLe() {
        Assert.assertEquals("name <= ?", Operator.le.buildCondition(STMT, PH));
    }

    @Test
    public void testContain() {
        Assert.assertTrue(Operator.contain.buildCondition(STMT, PH).contains("LIKE"));
        Assert.assertTrue(Operator.contain.buildCondition(STMT, PH).contains("CONCAT"));
    }

    @Test
    public void testNotContain() {
        Assert.assertTrue(Operator.not_contain.buildCondition(STMT, PH).contains("NOT LIKE"));
    }

    @Test
    public void testStartWith() {
        Assert.assertTrue(Operator.start_with.buildCondition(STMT, PH).contains("LIKE"));
    }

    @Test
    public void testEndWith() {
        Assert.assertTrue(Operator.end_with.buildCondition(STMT, PH).contains("LIKE"));
    }

    @Test
    public void testIn() {
        Assert.assertEquals("name IN (?)", Operator.in.buildCondition(STMT, PH));
    }

    @Test
    public void testNotIn() {
        Assert.assertEquals("name NOT IN (?)", Operator.not_in.buildCondition(STMT, PH));
    }

    @Test
    public void testIsNull() {
        Assert.assertEquals("name IS NULL ", Operator.is_null.buildCondition(STMT, PH));
    }

    @Test
    public void testIsNotNull() {
        Assert.assertEquals("name IS NOT NULL", Operator.is_not_null.buildCondition(STMT, PH));
    }

    @Test
    public void testImpossible() {
        Assert.assertEquals("1 != 1", Operator.impossible.buildCondition(STMT, PH));
    }

}
