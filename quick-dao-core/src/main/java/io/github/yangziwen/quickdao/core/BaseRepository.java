package io.github.yangziwen.quickdao.core;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface BaseRepository<E> extends BaseReadOnlyRepository<E> {

    int insert(E entity);

    default int batchInsert(List<E> entities) {
        return batchInsert(entities, entities.size());
    }

    int batchInsert(List<E> entities, int batchSize);

    int update(E entity);

    int updateSelective(E entity);

    int updateSelective(E entity, Criteria criteria);

    default int updateSelective(E entity, Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return updateSelective(entity, criteria);
    }

    int deleteById(Object id);

    int delete(Criteria criteria);

    int delete(Query query);

    int deleteByIds(Collection<?> ids);

    default int deleteCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return delete(criteria);
    }

    default int deleteQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return delete(query);
    }

}
