package io.github.yangziwen.quickdao.core;

import java.util.Collection;
import java.util.List;

public interface BaseRepository<E> extends BaseReadOnlyRepository<E> {

    void insert(E entity);

    default void batchInsert(List<E> entities) {
        batchInsert(entities, entities.size());
    }

    void batchInsert(List<E> entities, int batchSize);

    void update(E entity);

    void updateSelective(E entity);

    void deleteById(Object id);

    void delete(Criteria criteria);

    void delete(Query query);

    void deleteByIds(Collection<?> ids);

    default void deleteAll() {
        delete(Criteria.emptyCriteria());
    }

}
