package io.github.yangziwen.quickdao.core;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

public class TypedCriterion<E, V> extends Criterion<V> {

    private TypedCriteria<E> typedCriteria;

    TypedCriterion(String name, TypedCriteria<E> criteria) {
        this(name, criteria, true);
        this.typedCriteria = criteria;
    }

    TypedCriterion(String name, TypedCriteria<E> criteria, boolean valid) {
        super(name, criteria, valid);
        this.typedCriteria = criteria;
    }

    @Override
    protected TypedCriterion<E, V> autoEnd(boolean autoEnd) {
        super.autoEnd(autoEnd);
        return this;
    }

    @Override
    TypedCriteria<E> op(Operator operator, Object value) {
        super.op(operator, value);
        return isAutoEnd() ? typedCriteria.end() : typedCriteria;
    }

    @Override
    public TypedCriterion<E, V> jsonField(String jsonField) {
        super.jsonField(jsonField);
        return this;
    }

    @Override
    public TypedCriteria<E> eq(V value) {
        return op(Operator.eq, value);
    }

    @Override
    public TypedCriteria<E> ne(V value) {
        return op(Operator.ne, value);
    }

    @Override
    public TypedCriteria<E> gt(V value) {
        return op(Operator.gt, value);
    }

    @Override
    public TypedCriteria<E> ge(V value) {
        return op(Operator.ge, value);
    }

    @Override
    public TypedCriteria<E> lt(V value) {
        return op(Operator.lt, value);
    }

    @Override
    public TypedCriteria<E> le(V value) {
        return op(Operator.le, value);
    }

    @Override
    public TypedCriteria<E> contain(V value) {
        return op(Operator.contain, value);
    }

    @Override
    public TypedCriteria<E> notContain(V value) {
        return op(Operator.not_contain, value);
    }

    @Override
    public TypedCriteria<E> startWith(V value) {
        return op(Operator.start_with, value);
    }

    @Override
    public TypedCriteria<E> notStartWith(V value) {
        return op(Operator.not_start_with, value);
    }

    @Override
    public TypedCriteria<E> endWith(V value) {
        return op(Operator.end_with, value);
    }

    @Override
    public TypedCriteria<E> notEndWith(V value) {
        return op(Operator.not_end_with, value);
    }

    @Override
    public TypedCriteria<E> in(Object value) {
        return op(Operator.in, value);
    }

    @Override
    public TypedCriteria<E> in(V[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return op(Operator.impossible, null);
        }
        return op(Operator.in, values);
    }

    @Override
    public TypedCriteria<E> in(Collection<V> values) {
        if (CollectionUtils.isEmpty(values)) {
            return op(Operator.impossible, null);
        }
        return op(Operator.in, values);
    }

    @Override
    public TypedCriteria<E> notIn(Object value) {
        return op(Operator.not_in, value);
    }

    @Override
    public TypedCriteria<E> notIn(V[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return typedCriteria;
        }
        return op(Operator.not_in, values);
    }

    @Override
    public TypedCriteria<E> notIn(Collection<V> values) {
        if (CollectionUtils.isEmpty(values)) {
            return typedCriteria;
        }
        return op(Operator.not_in, values);
    }

    @Override
    public TypedCriteria<E> isNull() {
        return op(Operator.is_null, null);
    }

    @Override
    public TypedCriteria<E> isNotNull() {
        return op(Operator.is_not_null, null);
    }

}
