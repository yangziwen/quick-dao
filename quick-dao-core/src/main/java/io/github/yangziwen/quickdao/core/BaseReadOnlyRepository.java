package io.github.yangziwen.quickdao.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;

public interface BaseReadOnlyRepository<E> {

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
        Integer totalCount = count(query);
        List<E> list = totalCount > 0 ? list(query) : new ArrayList<>();
        return new Page<>(pageNo, pageSize, list, totalCount);
    }

    default Page<E> paginate(Criteria criteria, int pageNo, int pageSize) {
        return paginate(new Query().where(criteria), pageNo, pageSize);
    }

    default List<E> listQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return list(query);
    }

    default List<E> listCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return list(criteria);
    }

    default Integer countQuery(Consumer<TypedQuery<E>> consumer) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return count(query);
    }

    default Integer countCriteria(Consumer<TypedCriteria<E>> consumer) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return count(criteria);
    }

    default Page<E> paginateQuery(Consumer<TypedQuery<E>> consumer, int pageNo, int pageSize) {
        TypedQuery<E> query = newTypedQuery();
        consumer.accept(query);
        return paginate(query, pageNo, pageSize);
    }

    default Page<E> paginateCriteria(Consumer<TypedCriteria<E>> consumer, int pageNo, int pageSize) {
        TypedCriteria<E> criteria = newTypedCriteria();
        consumer.accept(criteria);
        return paginate(criteria, pageNo, pageSize);
    }

    TypedCriteria<E> newTypedCriteria();

    TypedQuery<E> newTypedQuery();

}
