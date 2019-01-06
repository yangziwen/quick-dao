package io.github.yangziwen.quickdao.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

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
    @Setter
    private String key = "";

    @Getter
    private List<Criterion> criterionList = new LinkedList<>();

    @Getter
    private Criteria parentCriteria = null;

    @Getter
    private Map<String, Criteria> nestedCriteriaMap = new LinkedHashMap<>();

    private AtomicInteger sequence = new AtomicInteger();

    public Criteria() {
        this(null, "");
    }

    public Criteria(Criteria parentCriteria, String key) {
        super();
        this.parentCriteria = parentCriteria;
        this.key = key;
    }

    public Criterion and(String name) {
        return new Criterion(name, this);
    }

    public Criterion or(String name) {
        return new Criterion(name, or());
    }

    public Criteria or() {
        String prefix = StringUtils.isNotBlank(key) ? key + RepoKeys.__ : "";
        return ensureNestedCriteria(prefix + getSequenceKey() + RepoKeys.OR);
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
            criteria = new Criteria(this, criteriaKey);
            nestedCriteriaMap.put(criteriaKey, criteria);
        }
        return criteria;
    }

    public static Criteria emptyCriteria() {
        return EMPTY_CRITERIA;
    }

    public Map<String, Object> toParamMap() {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        fillParamMap(paramMap);
        return paramMap;
    }

    protected void fillParamMap(Map<String, Object> paramMap) {
        for (Criterion criterion : criterionList) {
            paramMap.put(criterion.generatePlaceholderKey(), criterion.getValue());
        }
        if (MapUtils.isNotEmpty(nestedCriteriaMap)) {
            for (Criteria criteria : nestedCriteriaMap.values()) {
                criteria.fillParamMap(paramMap);
            }
        }
    }

    public String getSequenceKey() {
        int seq = sequence.getAndIncrement();
        StringBuilder buff = new StringBuilder();
        int begin = "a".charAt(0);
        int end = "z".charAt(0);
        int length = end + 1 - begin;
        buff.append((char) (seq % length + begin));
        while ((seq = seq / length) > 0) {
            buff.append((char) (seq % length + begin));
        }
        return buff.toString();
    }

}
