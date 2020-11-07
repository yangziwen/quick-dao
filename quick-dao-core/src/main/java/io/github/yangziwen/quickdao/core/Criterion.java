package io.github.yangziwen.quickdao.core;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.StringWrapper;
import lombok.Getter;
import lombok.Setter;

public class Criterion<V> {

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

    @Getter
    private boolean valid;

    @Getter
    private boolean autoEnd = false;

    Criterion(String name, Criteria criteria) {
        this(name, criteria, true);
    }

    Criterion(String name, Criteria criteria, boolean valid) {
        this.name = StringUtils.replacePattern(name, "\\s+", "");
        this.criteria = criteria;
        this.valid = valid;
    }

    Criteria op(Operator operator, Object value) {
        this.operator = operator;
        this.value = value;
        if (valid && !isRedundant()) {
            criteria.getCriterionList().add(this);
        }
        return autoEnd ? this.criteria.end() : this.criteria;
    }

    protected Criterion<V> autoEnd(boolean autoEnd) {
        this.autoEnd = autoEnd;
        return this;
    }

    private boolean isRedundant() {
        for (Criterion<?> criterion : criteria.getCriterionList()) {
            if (Objects.equals(criterion.getName(), getName())
                    && Objects.equals(criterion.getJsonField(), getJsonField())
                    // be aware that the value comparation is based on the shallow equals
                    && Objects.equals(criterion.getValue(), getValue())
                    && Objects.equals(criterion.getOperator(), getOperator())) {
                return true;
            }
        }
        return false;
    }

    public Criterion<?> jsonField(String jsonField) {
        this.jsonField = StringUtils.replacePattern(jsonField, "\\s+", "");
        return this;
    }

    public Criteria eq(V value) {
        return op(Operator.eq, value);
    }

    public Criteria ne(V value) {
        return op(Operator.ne, value);
    }

    public Criteria gt(V value) {
        return op(Operator.gt, value);
    }

    public Criteria ge(V value) {
        return op(Operator.ge, value);
    }

    public Criteria lt(V value) {
        return op(Operator.lt, value);
    }

    public Criteria le(V value) {
        return op(Operator.le, value);
    }

    public Criteria contain(V value) {
        return op(Operator.contain, value);
    }

    public Criteria notContain(V value) {
        return op(Operator.not_contain, value);
    }

    public Criteria startWith(V value) {
        return op(Operator.start_with, value);
    }

    public Criteria notStartWith(V value) {
        return op(Operator.not_start_with, value);
    }

    public Criteria endWith(V value) {
        return op(Operator.end_with, value);
    }

    public Criteria notEndWith(V value) {
        return op(Operator.not_end_with, value);
    }

    public Criteria in(Object value) {
        return op(Operator.in, value);
    }

    public Criteria in(Collection<V> values) {
        if (CollectionUtils.isEmpty(values)) {
            return op(Operator.impossible, null);
        }
        return op(Operator.in, values);
    }

    public Criteria notIn(Object value) {
        return op(Operator.not_in, value);
    }

    public Criteria notIn(Collection<V> values) {
        if (CollectionUtils.isEmpty(values)) {
            return criteria;
        }
        return op(Operator.not_in, values);
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
