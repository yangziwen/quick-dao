package io.github.yangziwen.quickdao.core;

import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public abstract class BaseCommonRepository<E> implements BaseReadOnlyRepository<E> {

    protected EntityMeta<E> entityMeta;

    protected final SqlGenerator sqlGenerator;

    protected BaseCommonRepository(SqlGenerator sqlGenerator) {
        this.entityMeta = EntityMeta
                .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public TypedCriteria<E> newTypedCriteria() {
        return new TypedCriteria<>(entityMeta.getClassType());
    }

    @Override
    public TypedQuery<E> newTypedQuery() {
        return new TypedQuery<>(entityMeta.getClassType());
    }

}
