package io.github.yangziwen.quickdao.core;

public class TypedCriterion<E> extends Criterion {

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
    TypedCriteria<E> op(Operator operator, Object value) {
        super.op(operator, value);
        return typedCriteria;
    }

    @Override
    public TypedCriteria<E> eq(Object value) {
        return op(Operator.eq, value);
    }

    @Override
    public TypedCriteria<E> ne(Object value) {
        return op(Operator.ne, value);
    }

    @Override
    public TypedCriteria<E> gt(Object value) {
        return op(Operator.gt, value);
    }

    @Override
    public TypedCriteria<E> ge(Object value) {
        return op(Operator.ge, value);
    }

    @Override
    public TypedCriteria<E> lt(Object value) {
        return op(Operator.lt, value);
    }

    @Override
    public TypedCriteria<E> le(Object value) {
        return op(Operator.le, value);
    }

    @Override
    public TypedCriteria<E> contain(Object value) {
        return op(Operator.contain, value);
    }

    @Override
    public TypedCriteria<E> notContain(Object value) {
        return op(Operator.not_contain, value);
    }

    @Override
    public TypedCriteria<E> startWith(Object value) {
        return op(Operator.start_with, value);
    }

    @Override
    public TypedCriteria<E> notStartWith(Object value) {
        return op(Operator.not_start_with, value);
    }

    @Override
    public TypedCriteria<E> endWith(Object value) {
        return op(Operator.end_with, value);
    }

    @Override
    public TypedCriteria<E> notEndWith(Object value) {
        return op(Operator.not_end_with, value);
    }

    @Override
    public TypedCriteria<E> in(Object value) {
        return op(Operator.in, value);
    }

    @Override
    public TypedCriteria<E> notIn(Object value) {
        return op(Operator.not_in, value);
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
