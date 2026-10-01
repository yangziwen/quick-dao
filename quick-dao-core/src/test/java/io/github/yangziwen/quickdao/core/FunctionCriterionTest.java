package io.github.yangziwen.quickdao.core;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.junit.Assert;
import org.junit.Test;

import io.github.yangziwen.quickdao.core.util.StringWrapper;

public class FunctionCriterionTest {

    private static final StringWrapper COLUMN_WRAPPER = new StringWrapper("`", "`");

    private static final StringWrapper PLACEHOLDER_WRAPPER = new StringWrapper(":", "");

    private final EntityMeta<TestUser> entityMeta = EntityMeta.newInstance(TestUser.class);

    @Test
    public void testAndExprMaxWithGetter() {
        TypedCriteria<TestUser> criteria = new TypedCriteria<>(TestUser.class);
        FunctionCriterion<TestUser> criterion = criteria.andExpr(expr -> expr.max(TestUser::getAge));
        criterion.gt(18);
        String condition = criterion.buildCondition(entityMeta, COLUMN_WRAPPER, PLACEHOLDER_WRAPPER);
        Assert.assertEquals(condition, true, condition.startsWith("MAX(`age`) > :"));
    }

    @Test
    public void testAndExprMinWithString() {
        TypedCriteria<TestUser> criteria = new TypedCriteria<>(TestUser.class);
        FunctionCriterion<TestUser> criterion = criteria.andExpr(expr -> expr.min("age"));
        criterion.ge(0);
        String condition = criterion.buildCondition(entityMeta, COLUMN_WRAPPER, PLACEHOLDER_WRAPPER);
        Assert.assertTrue(condition.contains("MIN(`age`) >= :"));
    }

    @Test
    public void testAndExprAvgAndSum() {
        assertExprCondition(expr -> expr.avg("age"), "AVG(`age`) < :",
                criterion -> criterion.lt(100));
        assertExprCondition(expr -> expr.sum("age"), "SUM(`age`) <= :",
                criterion -> criterion.le(100));
    }

    @Test
    public void testAndExprCountVariants() {
        assertExprCondition(expr -> expr.count(), "COUNT(*) = :",
                criterion -> criterion.eq(1));
        assertExprCondition(expr -> expr.count("age"), "COUNT(`age`) = :",
                criterion -> criterion.eq(1));
        assertExprCondition(expr -> expr.count(TestUser::getAge), "COUNT(`age`) != :",
                criterion -> criterion.ne(1));
        assertExprCondition(expr -> expr.countDistinct("age"), "COUNT(DISTINCT `age`) = :",
                criterion -> criterion.eq(1));
        assertExprCondition(expr -> expr.countDistinct(TestUser::getAge), "COUNT(DISTINCT `age`) = :",
                criterion -> criterion.eq(1));
    }

    @Test
    public void testAndExprDistinct() {
        assertExprCondition(expr -> expr.distinct("age"), "DISTINCT `age` = :",
                criterion -> criterion.eq(18));
        assertExprCondition(expr -> expr.distinct(TestUser::getAge), "DISTINCT `age` = :",
                criterion -> criterion.eq(18));
    }

    @Test
    public void testAndExprConcat() {
        assertExprCondition(expr -> expr.concat("`age`", "`id`"), "CONCAT(`age`, `id`) = :",
                criterion -> criterion.eq("x"));
        assertExprCondition(expr -> expr.concat(TestUser::getAge, TestUser::getId), "CONCAT(`age`, `id`) = :",
                criterion -> criterion.eq("x"));
    }

    @Test
    public void testAndExprWithUnknownColumnKeepsRawArg() {
        // "score" 不是 TestUser 的字段，render 时应保留原始参数名
        assertExprCondition(expr -> expr.max("score"), "MAX(score) > :",
                criterion -> criterion.gt(0));
    }

    @Test
    public void testOrExpr() {
        TypedCriteria<TestUser> criteria = new TypedCriteria<>(TestUser.class);
        criteria.and(TestUser::getAge).gt(60);
        FunctionCriterion<TestUser> criterion = criteria.orExpr(expr -> expr.max(TestUser::getAge));
        criterion.eq(100);
        String condition = criterion.buildCondition(entityMeta, COLUMN_WRAPPER, PLACEHOLDER_WRAPPER);
        Assert.assertTrue(condition.contains("MAX(`age`) = :"));
    }

    private void assertExprCondition(
            java.util.function.Consumer<SqlFunctionExpression<TestUser>> exprConsumer,
            String expectedExprPart,
            java.util.function.Consumer<FunctionCriterion<TestUser>> opConsumer) {
        TypedCriteria<TestUser> criteria = new TypedCriteria<>(TestUser.class);
        FunctionCriterion<TestUser> criterion = criteria.andExpr(exprConsumer);
        opConsumer.accept(criterion);
        String condition = criterion.buildCondition(entityMeta, COLUMN_WRAPPER, PLACEHOLDER_WRAPPER);
        Assert.assertTrue(condition, condition.startsWith(expectedExprPart));
    }

    @Table(name = "t_user")
    public static class TestUser {
        @Id
        @Column(name = "id")
        private Long id;
        @Column(name = "age")
        private Integer age;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

}
