package io.github.yangziwen.quickdao.core;

import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public abstract class BaseCommonRepository<E> implements BaseRepository<E> {

    protected EntityMeta<E> entityMeta;

    protected final SqlGenerator sqlGenerator;

    protected BaseCommonRepository(SqlGenerator sqlGenerator) {
        this.entityMeta = EntityMeta
                .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));
        this.sqlGenerator = sqlGenerator;
    }

    public TypedCriteria<E> newTypedCriteria() {
        return new TypedCriteria<E>(entityMeta.getClassType());
    }

}
