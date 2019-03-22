package io.github.yangziwen.quickdao.core;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public interface BaseRepository<E> {

    E getById(Object id);

    default E first(Query query) {
        List<E> list = list(query.limit(1));
        return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
    }

    default E first(Criteria criteria) {
        return first(new Query().where(criteria));
    }

    default List<E> list() {
        return list(new Query());
    }

    List<E> list(Query query);

    List<E> listByIds(Collection<?> ids);

    default List<E> list(Criteria criteria) {
        return list(new Query().where(criteria));
    }

    default Integer count() {
        return count(new Query());
    }

    Integer count(Query query);

    default Integer count(Criteria criteria) {
        return count(new Query().where(criteria));
    }

    default Page<E> paginate(Query query, int pageNo, int pageSize) {
        query.offset((pageNo - 1) * pageSize).limit(pageSize);
        List<E> list = list(query);
        Integer totalCount = count(query);
        return new Page<E>(pageNo, pageSize, list, totalCount);
    }

    default Page<E> paginate(Criteria criteria, int pageNo, int pageSize) {
        return paginate(new Query().where(criteria), pageNo, pageSize);
    }

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
