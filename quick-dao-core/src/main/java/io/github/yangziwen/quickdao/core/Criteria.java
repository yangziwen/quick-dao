package io.github.yangziwen.quickdao.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Getter;

public class Criteria {

    private static final Criteria EMPTY_CRITERIA = new Criteria() {

        @Override
        public Criterion and(String name) {
            throw new UnsupportedOperationException("and operation is not supported by empty criteria");
        }

        @Override
        public Criterion or(String name) {
            throw new UnsupportedOperationException("or operation is not supported by empty criteria");
        }

        @Override
        public Criteria or() {
            throw new UnsupportedOperationException("or operation is not supported by empty criteria");
        }

    };

    @Getter
    private List<Criterion> criterionList = new LinkedList<>();

    private Criteria parentCriteria = null;

    private Map<String, Criteria> nestedCriteriaMap = new LinkedHashMap<>();

    private AtomicInteger sequence = new AtomicInteger();

    public Criteria() {
        this(null);
    }

    public Criteria(Criteria parentCriteria) {
        super();
        this.parentCriteria = parentCriteria;
    }

    public Criterion and(String name) {
        return new Criterion(name, this);
    }

    public Criterion or(String name) {
        return new Criterion(name, or());
    }

    public Criteria or() {
        return ensureNestedCriteria(sequence.getAndIncrement() + RepoKeys.OR);
    }

    public Criteria end() {
        return Optional.ofNullable(parentCriteria).orElse(this);
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(criterionList);
    }

    private Criteria ensureNestedCriteria(String criteriaKey) {
        Criteria criteria = nestedCriteriaMap.get(criteriaKey);
        if (criteria == null) {
            criteria = new Criteria(this);
            nestedCriteriaMap.put(criteriaKey, criteria);
        }
        return criteria;
    }

    public static Criteria emptyCriteria() {
        return EMPTY_CRITERIA;
    }

}
