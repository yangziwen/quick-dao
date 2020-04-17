package io.github.yangziwen.quickdao.core;

import java.util.List;
import java.util.function.Consumer;

import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public abstract class BaseCommonRepository<E> implements BaseReadOnlyRepository<E> {

    protected EntityMeta<E> entityMeta;

    protected final SqlGenerator sqlGenerator;

    protected BaseCommonRepository(SqlGenerator sqlGenerator) {
        this.entityMeta = EntityMeta
                .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));
        this.sqlGenerator = sqlGenerator;
    }

    public TypedCriteria<E> newTypedCriteria() {
        return new TypedCriteria<>(entityMeta.getClassType());
    }

    public TypedQuery<E> newTypedQuery() {
        return new TypedQuery<>(entityMeta.getClassType());
    }

    public List<E> listQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return list(query);
    }

    public List<E> listCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return list(criteria);
    }

    public Integer countQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return count(query);
    }

    public Integer countCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return count(criteria);
    }

    public Page<E> paginateQuery(Consumer<TypedQuery<E>> consumer, int pageNo, int pageSize) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return paginate(query, pageNo, pageSize);
    }

    public Page<E> paginateCriteria(Consumer<TypedCriteria<E>> consumer, int pageNo, int pageSize) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return paginate(criteria, pageNo, pageSize);
    }

}
