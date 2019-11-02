package io.github.yangziwen.quickdao.core;

import java.util.function.Function;

import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;
import lombok.Getter;

public class TypedQuery<E> extends Query {

    private static final long serialVersionUID = 1L;

    private final InvokedMethodExtractor<E> extractor;

    @Getter
    private TypedCriteria<E> criteria;

    @Getter
    private TypedCriteria<E> havingCriteria;


    public TypedQuery(Class<E> classType) {
        this.extractor = new InvokedMethodExtractor<>(classType);
        this.criteria = new TypedCriteria<>(classType);
        this.havingCriteria = new TypedCriteria<>(classType, null, 0 + RepoKeys.HAVING);
        super.where(this.criteria);
        super.having(this.havingCriteria);
    }

    @Override
    public TypedQuery<E> select(String...fields) {
        super.select(fields);
        return this;
    }

    public TypedQuery<E> select(Function<E, ?> getter) {
        return this.select(extractor.extractFieldNameFromGetter(getter));
    }

    public TypedQuery<E> where(TypedCriteria<E> criteria) {
        super.where(this.criteria = criteria);
        return this;
    }

    @Override
    public TypedQuery<E> groupBy(String stmt) {
        super.groupBy(stmt);
        return this;
    }

    public TypedQuery<E> groupBy(Function<E, ?> getter) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return this.groupBy(name);
    }

    public TypedQuery<E> having(TypedCriteria<E> criteria) {
        criteria.setKey(RepoKeys.HAVING);
        super.having(this.havingCriteria = criteria);
        return this;
    }

    @Override
    public TypedQuery<E> orderBy(String name, Direction direction) {
        super.orderBy(name, direction);
        return this;
    }

    public TypedQuery<E> orderBy(Function<E, ?> getter, Direction direction) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return this.orderBy(name, direction);
    }

    @Override
    public TypedQuery<E> orderBy(String name) {
        return this.orderBy(name, Direction.ASC);
    }

    public TypedQuery<E> orderBy(Function<E, ?> getter) {
        return this.orderBy(getter, Direction.ASC);
    }

    @Override
    public TypedQuery<E> offset(int offset) {
        super.offset(offset);
        return this;
    }

    @Override
    public TypedQuery<E> limit(int limit) {
        super.limit(limit);
        return this;
    }

}
