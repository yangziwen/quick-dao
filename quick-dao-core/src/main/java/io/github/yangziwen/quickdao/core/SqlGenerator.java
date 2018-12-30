package io.github.yangziwen.quickdao.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public class SqlGenerator {

    private PlaceholderWrapper wrapper;

    public SqlGenerator(PlaceholderWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public <T> String generateUpdateSql(EntityMeta<T> entityMeta) {

        StringBuilder buff = new StringBuilder(" UPDATE ").append(entityMeta.getTable());

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        buff.append(entityMeta.getColumnNameByField(fields.get(0)))
            .append(" = ")
            .append(wrapper.wrap(fields.get(0).getName()));

        buff.append(" SET ");

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ")
                .append(entityMeta.getColumnNameByField(fields.get(i)))
                .append(" = ")
                .append(wrapper.wrap(fields.get(i).getName()));
        }

        buff.append(" WHERE ").append(entityMeta.getIdColumnName())
            .append(" = ")
            .append(wrapper.wrap(entityMeta.getIdFieldName()));

        return buff.toString();
    }

    public <T> String generateUpdateSelectiveSql(T entity, EntityMeta<T> entityMeta) {

        StringBuilder buff = new StringBuilder(" UPDATE ").append(entityMeta.getTable());

        buff.append(" SET ");

        int i = 0;

        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            if (ReflectionUtil.getFieldValue(entity, field) == null) {
                continue;
            }
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(entityMeta.getColumnNameByField(field))
                .append(" = ")
                .append(wrapper.wrap(field.getName()));
        }

        buff.append(" WHERE ").append(entityMeta.getIdColumnName())
            .append(" = ")
            .append(wrapper.wrap(entityMeta.getIdFieldName()));

        return buff.toString();
    }


    public <T> String generateInsertSql(EntityMeta<T> entityMeta) {

        StringBuilder buff = new StringBuilder(" INSERT INTO ").append(entityMeta.getTable());

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        List<String> columnNames = entityMeta.getColumnNamesByFields(fields);

        buff.append(" ( ").append(columnNames.get(0));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(columnNames.get(i));
        }

        buff.append(" ) VALUES ( ").append(wrapper.wrap(fields.get(0).getName()));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(wrapper.wrap(fields.get(i).getName()));
        }

        buff.append(" ) ");

        return buff.toString();

    }

    public <T> String generateBatchInsertSql(EntityMeta<T> entityMeta, int batchSize) {

        List<Field> fields = entityMeta.getFieldsWithoutIdField();

        List<String> columnNames = entityMeta.getColumnNamesByFields(fields);

        StringBuilder buff = new StringBuilder().append(" INSERT INTO ").append(entityMeta.getTable());

        buff.append(" ( ").append(columnNames.get(0));

        for (int i = 1; i < fields.size(); i++) {
            buff.append(", ").append(columnNames.get(i));
        }

        buff.append(" ) VALUES ");

        for (int i = 0; i < batchSize; i++) {
            buff.append("( ").append(wrapper.wrap(i, fields.get(0).getName()));
            for (int j = 1; j < columnNames.size(); j++) {
                buff.append(", ").append(wrapper.wrap(i, fields.get(j).getName()));
            }
            buff.append(" ) ");
            if (i < batchSize - 1) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }

    public <T> String generateDeleteByIdSql(EntityMeta<T> entityMeta) {
        return  " DELETE FROM " + entityMeta.getTable() +
                " WHERE " + entityMeta.getIdColumnName() + " = " + wrapper.wrap(entityMeta.getIdFieldName());
    }

    public <T> String generateDeleteByCriteriaSql(EntityMeta<T> entityMeta, Criteria criteria) {
        return null;
    }

    public <T> String generateListByQuerySql(EntityMeta<T> entityMeta, Query query) {
        StringBuilder buff = new StringBuilder();
        appendSelect(buff, entityMeta, query);
        appendFrom(buff, entityMeta);
        appendWhere(buff, entityMeta, query.getCriteria());
        appendGroupBy(buff, entityMeta, query);
        appendHaving(buff, entityMeta, query.getCriteria());
        appendOrderBy(buff, entityMeta, query);
        appendLimit(buff, entityMeta, query);
        return buff.toString();
    }

    public <T> String generateCountByQuerySql(EntityMeta<T> entityMeta,Query query) {
        StringBuilder buff = new StringBuilder();
        buff.append(" SELECT COUNT(*) ");
        if (CollectionUtils.isNotEmpty(query.getGroupByList())) {
            buff.append(" FROM ( SELECT 1 ");
            appendFrom(buff, entityMeta);
            appendWhere(buff, entityMeta, query.getCriteria());
            buff.append(" ) result ");
        } else {
            appendFrom(buff, entityMeta);
            appendWhere(buff, entityMeta, query.getCriteria());
        }
        return buff.toString();
    }

    private <T> void appendSelect(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        buff.append(" SELECT ");
        if (CollectionUtils.isNotEmpty(query.getFieldList())) {
            buff.append(query.getFieldList().get(0));
            for (int i = 1; i < query.getFieldList().size(); i++) {
                buff.append(", ").append(query.getFieldList().get(i));
            }
            return;
        }
        int i = 0;
        for (String stmt : entityMeta.getSelectStmts()) {
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(stmt);
        }
    }

    private <T> void appendFrom(StringBuilder buff, EntityMeta<T> entityMeta) {
        buff.append(" FROM ").append(entityMeta.getTable());
    }

    private <T> void appendWhere(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        buff.append(" WHERE ");
        appendConditions(buff, entityMeta, criteria);
    }

    private <T> void appendConditions(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        buff.append(" ( ");
        int i = 0;
        for (Criterion criterion : criteria.getCriterionList()) {
            if (i++ > 0) {
                buff.append(" AND ");
            }
            if (criterion.getValue() instanceof Object[]) {
                criterion.setValue(Arrays.asList((Object[]) criterion.getValue()));
            }
            buff.append(criterion.buildCondition(entityMeta, wrapper));
        }
        buff.append(") ");
        for (Entry<String, Criteria> entry : criteria.getNestedCriteriaMap().entrySet()) {
            if (entry.getKey().endsWith(RepoKeys.OR)) {
                buff.append(" OR ");
                appendConditions(buff, entityMeta, entry.getValue());
            }
        }
    }

    private <T> void appendGroupBy(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        if (CollectionUtils.isEmpty(query.getGroupByList())) {
            return;
        }
        buff.append(" GROUP BY ");
        int i = 0;
        for (String fieldName : query.getGroupByList()) {
            String columnName = entityMeta.getColumnNameByFieldName(fieldName);
            if (StringUtils.isBlank(columnName)) {
                columnName = fieldName;
            }
            if (i++ > 0) {
                buff.append(", ");
            }
            buff.append(columnName);
        }
    }

    private <T> void appendHaving(StringBuilder buff, EntityMeta<T> entityMeta, Criteria criteria) {
        buff.append(" HAVING ");
        appendConditions(buff, entityMeta, criteria);
    }

    private <T> void appendOrderBy(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        if (CollectionUtils.isEmpty(query.getOrderList())) {
            return;
        }
        buff.append(" ORDER BY ");
        int i = 0;
        for (Order order : query.getOrderList()) {
            String columnName = entityMeta.getColumnNameByFieldName(order.getName());
            if (StringUtils.isBlank(columnName)) {
                columnName = order.getName();
            }
            if (i++ > 0) {
                buff.append(",");
            }
            buff.append(columnName).append(" ").append(order.getDirection().name().toLowerCase());
        }
    }

    private <T> void appendLimit(StringBuilder buff, EntityMeta<T> entityMeta, Query query) {
        int offset = query.getOffset();
        int limit = query.getLimit();
        if (limit <= 0) {
            return;
        }
        if (offset < 0) {
            offset = 0;
        }
        buff.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
    }

}
