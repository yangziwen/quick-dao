package io.github.yangziwen.quickdao.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;

public class TypedCriteria<E> extends Criteria {

    private final InvokedMethodExtractor<E> extractor;

    private final Class<E> classType;

    public TypedCriteria(Class<E> classType) {
        super();
        this.classType = classType;
        this.extractor = new InvokedMethodExtractor<>(classType);
    }

    public TypedCriteria(Class<E> classType, Criteria parentCriteria, String key) {
        super(parentCriteria, key);
        this.classType = classType;
        this.extractor = new InvokedMethodExtractor<>(classType);
    }

    public InnerCriteria ifValid(BooleanSupplier supplier) {
        return new InnerCriteria(supplier.getAsBoolean());
    }

    @Override
    public TypedCriterion<E> and(String name) {
        return new TypedCriterion<>(name, this);
    }

    public TypedCriterion<E> and(Function<E, ?> getter) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return new TypedCriterion<>(name, this);
    }

    public TypedCriterion<E> or(Function<E, ?> getter) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return new TypedCriterion<>(name, or());
    }

    @Override
    public TypedCriterion<E> or(String name) {
        return new TypedCriterion<>(name, or());
    }

    @Override
    public TypedCriteria<E> and() {
        String prefix = StringUtils.isNotBlank(getKey()) ? getKey() + RepoKeys.__ : "";
        return ensureNestedCriteria(prefix + getSequenceKey() + RepoKeys.AND);
    }

    @Override
    public TypedCriteria<E> or() {
        String prefix = StringUtils.isNotBlank(getKey()) ? getKey() + RepoKeys.__ : "";
        return ensureNestedCriteria(prefix + getSequenceKey() + RepoKeys.OR);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypedCriteria<E> end() {
        return (TypedCriteria<E>) Optional.ofNullable(getParentCriteria()).orElse(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TypedCriteria<E> ensureNestedCriteria(String criteriaKey) {
        TypedCriteria<E> criteria = (TypedCriteria<E>) getNestedCriteriaMap().get(criteriaKey);
        if (criteria == null) {
            criteria = new TypedCriteria<>(classType, this, criteriaKey);
            getNestedCriteriaMap().put(criteriaKey, criteria);
        }
        return criteria;
    }

    public static <T> TypedCriteria<T> fromParamMap(Class<T> classType, Map<String, Object> paramMap) {
        TypedCriteria<T> criteria = new TypedCriteria<>(classType);
        if (MapUtils.isEmpty(paramMap)) {
            return criteria;
        }
        String orSep = RepoKeys.OR + RepoKeys.__;
        for (Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            TypedCriteria<T> currentCriteria = criteria;
            if (key.contains(orSep)) {
                int fromIndex = 0;
                int pos = -1;
                while ((pos = key.indexOf(orSep, fromIndex)) >= 0) {
                    String criteriaKey = key.substring(0, pos + RepoKeys.OR.length());
                    currentCriteria = currentCriteria.ensureNestedCriteria(criteriaKey);
                    fromIndex = pos + orSep.length();
                }
                key = key.substring(fromIndex);
            }
            String[] arr = key.split(RepoKeys.__);
            String name = arr[0];
            Operator operator = arr.length >= 2 ? Operator.valueOf(arr[1]) : Operator.eq;
            new TypedCriterion<T>(name, currentCriteria).op(operator, value);
        }
        return criteria;
    }

    public class InnerCriteria {

        boolean valid;

        public InnerCriteria(boolean valid) {
            this.valid = valid;
        }

        public TypedCriterion<E> then(Function<E, ?> getter) {
            String name = extractor.extractFieldNameFromGetter(getter);
            return new TypedCriterion<>(name, TypedCriteria.this, valid);
        }

    }

}
