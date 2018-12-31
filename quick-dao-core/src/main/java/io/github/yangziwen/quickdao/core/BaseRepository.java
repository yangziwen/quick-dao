package io.github.yangziwen.quickdao.core;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public interface BaseRepository<E> {

    default E first(Query query) {
        List<E> list = list(query.limit(1));
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    default List<E> list() {
        return list(new Query());
    }

    List<E> list(Query query);

    default Integer count() {
        return count(new Query());
    }

    Integer count(Query query);

    void insert(E entity);

    default void batchInsert(List<E> entities) {
        batchInsert(entities, entities.size());
    }

    void batchInsert(List<E> entities, int batchSize);

    void update(E entity);

    void updateSelective(E entity);

    void deleteById(Object id);

    void delete(Criteria criteria);

    default void deleteAll() {
        delete(Criteria.emptyCriteria());
    }

}
