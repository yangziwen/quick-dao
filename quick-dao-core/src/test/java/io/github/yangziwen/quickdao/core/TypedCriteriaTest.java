package io.github.yangziwen.quickdao.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TypedCriteriaTest {

    @Test
    public void testIfValidWithTrueConditionAddsCriterion() {
        TypedCriteria<FunctionCriterionTest.TestUser> criteria
                = new TypedCriteria<>(FunctionCriterionTest.TestUser.class);
        criteria.ifValid(() -> true).then(FunctionCriterionTest.TestUser::getAge).gt(18);
        criteria.ifValid(true).then("age").lt(60);

        Assert.assertEquals(2, criteria.getCriterionList().size());
        Map<String, Object> paramMap = criteria.toParamMap();
        Assert.assertEquals(18, paramMap.get("age__gt"));
        Assert.assertEquals(60, paramMap.get("age__lt"));
    }

    @Test
    public void testIfValidWithFalseConditionSkipsCriterion() {
        TypedCriteria<FunctionCriterionTest.TestUser> criteria
                = new TypedCriteria<>(FunctionCriterionTest.TestUser.class);
        criteria.ifValid(() -> false).then(FunctionCriterionTest.TestUser::getAge).gt(18);
        Assert.assertEquals(0, criteria.getCriterionList().size());
        Assert.assertTrue(criteria.toParamMap().isEmpty());
    }

    @Test
    public void testUntypedIfValid() {
        Criteria criteria = new Criteria();
        criteria.ifValid(true).then("age").gt(18);
        criteria.ifValid(false).then("age").lt(60);
        Assert.assertEquals(1, criteria.getCriterionList().size());
    }

    @Test
    public void testFromParamMapRoundTrip() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("age__gt", 18);
        paramMap.put("id__eq", 1L);
        paramMap.put("age2__le", 30);

        TypedCriteria<FunctionCriterionTest.TestUser> criteria
                = TypedCriteria.fromParamMap(FunctionCriterionTest.TestUser.class, paramMap);
        Assert.assertEquals(3, criteria.getCriterionList().size());

        Map<String, Object> result = criteria.toParamMap();
        Assert.assertEquals(18, result.get("age__gt"));
        Assert.assertEquals(1L, result.get("id__eq"));
        Assert.assertEquals(30, result.get("age2__le"));
    }

    @Test
    public void testFromParamMapWithEmptyMap() {
        TypedCriteria<FunctionCriterionTest.TestUser> criteria
                = TypedCriteria.fromParamMap(FunctionCriterionTest.TestUser.class, new HashMap<>());
        Assert.assertEquals(0, criteria.getCriterionList().size());
    }

    @Test
    public void testFromParamMapWithNestedKey() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("a__and__age__gt", 18);
        paramMap.put("b__or__id__eq", 1L);

        TypedCriteria<FunctionCriterionTest.TestUser> criteria
                = TypedCriteria.fromParamMap(FunctionCriterionTest.TestUser.class, paramMap);
        // 嵌套条件应挂在嵌套 criteria 上，而不是根 criteria
        Assert.assertEquals(0, criteria.getCriterionList().size());
        Assert.assertEquals(2, criteria.getNestedCriteriaMap().size());
        Assert.assertEquals(18, criteria.toParamMap().get("a__and__age__gt"));
        Assert.assertEquals(1L, criteria.toParamMap().get("b__or__id__eq"));
    }

}
