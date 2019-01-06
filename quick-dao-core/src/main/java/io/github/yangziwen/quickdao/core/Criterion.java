package io.github.yangziwen.quickdao.core;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.StringWrapper;
import lombok.Getter;
import lombok.Setter;

public class Criterion {

    private Criteria criteria;

    @Getter
    private String name;

    @Getter
    @Setter
    private Object value;

    @Getter
    private Operator operator = Operator.eq;

    Criterion(String name, Criteria criteria) {
        this(name, criteria, true);
    }

    Criterion(String name, Criteria criteria, boolean isValid) {
        this.name = name;
        this.criteria = criteria;
        if (isValid) {
            criteria.getCriterionList().add(this);
        }
    }

    public Criteria eq(Object value) {
        this.operator = Operator.eq;
        this.value = value;
        return this.criteria;
    }

    public Criteria ne(Object value) {
        this.operator = Operator.ne;
        this.value = value;
        return this.criteria;
    }

    public Criteria gt(Object value) {
        this.operator = Operator.gt;
        this.value = value;
        return this.criteria;
    }

    public Criteria ge(Object value) {
        this.operator = Operator.ge;
        this.value = value;
        return this.criteria;
    }

    public Criteria lt(Object value) {
        this.operator = Operator.lt;
        this.value = value;
        return this.criteria;
    }

    public Criteria le(Object value) {
        this.operator = Operator.le;
        this.value = value;
        return this.criteria;
    }

    public Criteria contain(Object value) {
        this.operator = Operator.contain;
        this.value = value;
        return this.criteria;
    }

    public Criteria notContain(Object value) {
        this.operator = Operator.not_contain;
        this.value = value;
        return this.criteria;
    }

    public Criteria startWith(Object value) {
        this.operator = Operator.start_with;
        this.value = value;
        return this.criteria;
    }

    public Criteria notStartWith(Object value) {
        this.operator = Operator.not_start_with;
        this.value = value;
        return this.criteria;
    }

    public Criteria endWith(Object value) {
        this.operator = Operator.end_with;
        this.value = value;
        return this.criteria;
    }

    public Criteria notEndWith(Object value) {
        this.operator = Operator.not_end_with;
        this.value = value;
        return this.criteria;
    }

    public Criteria in(Object value) {
        this.operator = Operator.in;
        this.value = value;
        return this.criteria;
    }

    public Criteria notIn(Object value) {
        this.operator = Operator.not_in;
        this.value = value;
        return this.criteria;
    }

    public Criteria isNull() {
        this.operator = Operator.is_null;
        return this.criteria;
    }

    public Criteria isNotNull() {
        this.operator = Operator.is_not_null;
        return this.criteria;
    }

    public String generatePlaceholderKey() {
        String prefix = StringUtils.isNotBlank(criteria.getKey()) ? criteria.getKey() + RepoKeys.__ : "";
        return prefix + name + RepoKeys.__ + operator.name();
    }

    public <T> String buildCondition(EntityMeta<T> entityMeta, StringWrapper columnWrapper, StringWrapper placeholderWrapper) {
        String columnName = entityMeta.getColumnNameByFieldName(name);
        String stmt = StringUtils.isNotBlank(columnName)
                ? columnWrapper.wrap(columnName) : name;
        String placeholder = placeholderWrapper.wrap(generatePlaceholderKey());
        return operator.buildCondition(stmt, placeholder);
    }

}
