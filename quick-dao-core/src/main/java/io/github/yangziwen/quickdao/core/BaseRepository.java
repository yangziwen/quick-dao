package io.github.yangziwen.quickdao.core;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface BaseRepository<E> extends BaseReadOnlyRepository<E> {

    void insert(E entity);

    default void batchInsert(List<E> entities) {
        batchInsert(entities, entities.size());
    }

    void batchInsert(List<E> entities, int batchSize);

    void update(E entity);

    void updateSelective(E entity);

    void updateSelective(E entity, Criteria criteria);

    default void updateSelective(E entity, Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        updateSelective(entity, criteria);
    }

    void deleteById(Object id);

    void delete(Criteria criteria);

    void delete(Query query);

    void deleteByIds(Collection<?> ids);

    default void deleteCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        delete(criteria);
    }

    default void deleteQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        delete(query);
    }

    default void deleteAll() {
        delete(Criteria.emptyCriteria());
    }

}
