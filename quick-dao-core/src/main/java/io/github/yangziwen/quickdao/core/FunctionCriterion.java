package io.github.yangziwen.quickdao.core;

import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;
import io.github.yangziwen.quickdao.core.util.StringWrapper;
import lombok.Getter;

@Getter
public class FunctionCriterion<E> extends TypedCriterion<E, Object> {

    private final InvokedMethodExtractor<E> extractor;

    private final SqlFunctionExpression<E> expression;

    FunctionCriterion(
            SqlFunctionExpression<E> expression,
            InvokedMethodExtractor<E> extractor,
            TypedCriteria<E> criteria) {
        super(expression.getFunc().getName() + "_" + expression.hashCode(), criteria);
        this.expression = expression;
        this.extractor = extractor;
    }

    @Override
    protected FunctionCriterion<E> autoEnd(boolean autoEnd) {
        super.autoEnd(autoEnd);
        return this;
    }

    @Override
    public <T> String buildCondition(
            EntityMeta<T> entityMeta,
            StringWrapper columnWrapper,
            StringWrapper placeholderWrapper) {
        String expr = getExpression().render(entityMeta, extractor, columnWrapper);
        String placeholder = placeholderWrapper.wrap(generatePlaceholderKey());
        return getOperator().buildCondition(expr, placeholder);
    }

}
