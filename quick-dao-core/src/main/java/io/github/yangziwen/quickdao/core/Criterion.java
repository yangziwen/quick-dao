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
    private String jsonField;

    @Getter
    @Setter
    private Object value;

    @Getter
    private Operator operator = Operator.eq;

    Criterion(String name, Criteria criteria) {
        this(name, criteria, true);
    }

    Criterion(String name, Criteria criteria, boolean valid) {
        this.name = StringUtils.replacePattern(name, "\\s+", "");
        this.criteria = criteria;
        if (valid) {
            criteria.getCriterionList().add(this);
        }
    }

    Criteria op(Operator operator, Object value) {
        this.operator = operator;
        this.value = value;
        return this.criteria;
    }

    public Criterion jsonField(String jsonField) {
        this.jsonField = StringUtils.replacePattern(jsonField, "\\s+", "");
        return this;
    }

    public Criteria eq(Object value) {
        return op(Operator.eq, value);
    }

    public Criteria ne(Object value) {
        return op(Operator.ne, value);
    }

    public Criteria gt(Object value) {
        return op(Operator.gt, value);
    }

    public Criteria ge(Object value) {
        return op(Operator.ge, value);
    }

    public Criteria lt(Object value) {
        return op(Operator.lt, value);
    }

    public Criteria le(Object value) {
        return op(Operator.le, value);
    }

    public Criteria contain(Object value) {
        return op(Operator.contain, value);
    }

    public Criteria notContain(Object value) {
        return op(Operator.not_contain, value);
    }

    public Criteria startWith(Object value) {
        return op(Operator.start_with, value);
    }

    public Criteria notStartWith(Object value) {
        return op(Operator.not_start_with, value);
    }

    public Criteria endWith(Object value) {
        return op(Operator.end_with, value);
    }

    public Criteria notEndWith(Object value) {
        return op(Operator.not_end_with, value);
    }

    public Criteria in(Object value) {
        return op(Operator.in, value);
    }

    public Criteria notIn(Object value) {
        return op(Operator.not_in, value);
    }

    public Criteria isNull() {
        return op(Operator.is_null, null);
    }

    public Criteria isNotNull() {
        return op(Operator.is_not_null, null);
    }

    public String generatePlaceholderKey() {
        String prefix = StringUtils.isNotBlank(criteria.getKey()) ? criteria.getKey() + RepoKeys.__ : "";
        String jsonFieldSuffix = StringUtils.isNotEmpty(jsonField) ? (RepoKeys.JSON_FIELD + jsonField) : "";
        return prefix + name + jsonFieldSuffix + RepoKeys.__ + operator.name();
    }

    public <T> String buildCondition(EntityMeta<T> entityMeta, StringWrapper columnWrapper, StringWrapper placeholderWrapper) {
        String columnName = entityMeta.getColumnNameByFieldName(name);
        String stmt = StringUtils.isNotBlank(columnName)
                ? columnWrapper.wrap(columnName) : name;
        if (StringUtils.isNotBlank(jsonField)) {
            stmt += "->'$." + jsonField + "'";
        }
        String placeholder = placeholderWrapper.wrap(generatePlaceholderKey());
        return operator.buildCondition(stmt, placeholder);
    }

}
