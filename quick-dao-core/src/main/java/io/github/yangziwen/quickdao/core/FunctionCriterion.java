package io.github.yangziwen.quickdao.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;
import io.github.yangziwen.quickdao.core.util.StringWrapper;
import lombok.Getter;

@Getter
public class FunctionCriterion<E> extends TypedCriterion<E> {

    private final InvokedMethodExtractor<E> extractor;

    private final SqlFunctionExpression<E> expression;

    FunctionCriterion(
            SqlFunctionExpression<E> expression,
            InvokedMethodExtractor<E> extractor,
            TypedCriteria<E> criteria) {
        super(String.valueOf(expression), criteria);
        this.expression = expression;
        this.extractor = extractor;
    }

    @Override
    protected FunctionCriterion<E> autoEnd(boolean autoEnd) {
        super.autoEnd(autoEnd);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> String buildCondition(
            EntityMeta<T> entityMeta,
            StringWrapper columnWrapper,
            StringWrapper placeholderWrapper) {
        List<String> args = new ArrayList<>();
        if (getExpression().getArgs() instanceof String[]) {
            for (String arg : (String[]) getExpression().getArgs()) {
                String column = entityMeta.getColumnNameByFieldName(arg);
                if (StringUtils.isBlank(column)) {
                    args.add(arg);
                } else {
                    args.add(columnWrapper.wrap(column));
                }
            }
        } else if (getExpression().getArgs() instanceof Function[]) {
            for (Function<E, ?> getter : (Function[]) getExpression().getArgs()) {
                String field = getExtractor().extractFieldNameFromGetter(getter);
                String column = entityMeta.getColumnNameByFieldName(field);
                args.add(columnWrapper.wrap(column));
            }
        }
        String expr = getExpression().getFunc().render(args.toArray(ArrayUtils.EMPTY_OBJECT_ARRAY));
        String placeholder = placeholderWrapper.wrap(generatePlaceholderKey());
        return getOperator().buildCondition(expr, placeholder);
    }

}
