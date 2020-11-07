package io.github.yangziwen.quickdao.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        public <V> Criterion<V> and(String name) {
            throw new UnsupportedOperationException("and operation is not supported by empty criteria");
        }

        @Override
        public <V> Criterion<V> or(String name) {
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
    private List<Criterion<?>> criterionList = new LinkedList<>();

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

    public InnerCriteria ifValid(boolean valid) {
        return new InnerCriteria(valid);
    }

    public <V> Criterion<V> and(String name) {
        return new Criterion<V>(name, this);
    }

    public <V> Criterion<V> or(String name) {
        return new Criterion<V>(name, or()).autoEnd(true);
    }

    public Criteria and() {
        String prefix = StringUtils.isNotBlank(key) ? key + RepoKeys.__ : "";
        return ensureNestedCriteria(prefix + getSequenceKey() + RepoKeys.AND);
    }

    public Criteria or() {
        String prefix = StringUtils.isNotBlank(key) ? key + RepoKeys.__ : "";
        return ensureNestedCriteria(prefix + getSequenceKey() + RepoKeys.OR);
    }

    public Criteria end() {
        return Optional.ofNullable(parentCriteria).orElse(this);
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(criterionList) && MapUtils.isEmpty(nestedCriteriaMap);
    }

    protected Criteria ensureNestedCriteria(String criteriaKey) {
        return nestedCriteriaMap.computeIfAbsent(criteriaKey, ck -> new Criteria(this, ck));
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
        for (Criterion<?> criterion : criterionList) {
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

    public static Criteria fromParamMap(Map<String, Object> paramMap) {
        Criteria criteria = new Criteria();
        if (MapUtils.isEmpty(paramMap)) {
            return criteria;
        }
        String andSep = RepoKeys.AND + RepoKeys.__;
        String orSep = RepoKeys.OR + RepoKeys.__;
        for (Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Criteria currentCriteria = criteria;
            if (key.contains(orSep)) {
                int fromIndex = 0;
                int andPos = -1;
                int orPos = -1;
                while ((andPos = key.indexOf(andPos, fromIndex)) >= 0
                        || (orPos = key.indexOf(orSep, fromIndex)) >= 0) {
                    String keyword = RepoKeys.OR;
                    int pos = orPos;
                    String sep = orSep;
                    if (andPos > -1 && andPos < orPos) {
                        keyword = RepoKeys.AND;
                        pos = andPos;
                        sep = andSep;
                    }
                    String criteriaKey = key.substring(0, pos + keyword.length());
                    currentCriteria = currentCriteria.ensureNestedCriteria(criteriaKey);
                    fromIndex = pos + sep.length();
                }
                key = key.substring(fromIndex);
            }
            String[] arr = key.split(RepoKeys.__);
            String name = arr[0];
            String jsonField = "";
            Operator operator = arr.length >= 2 ? Operator.valueOf(arr[1]) : Operator.eq;
            if (name.contains(RepoKeys.JSON_FIELD)) {
                String[] nameArr = StringUtils.splitByWholeSeparator(name, RepoKeys.JSON_FIELD, 2);
                name = nameArr[0];
                jsonField = nameArr[1];
            }
            new Criterion<>(name, currentCriteria).jsonField(jsonField).op(operator, value);
        }
        return criteria;
    }

    public class InnerCriteria {

        boolean valid;

        public InnerCriteria(boolean valid) {
            this.valid = valid;
        }

        public Criterion<?> then(String name) {
            return new Criterion<>(name, Criteria.this, valid);
        }

    }

}
