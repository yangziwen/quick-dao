package io.github.yangziwen.quickdao.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.util.StringWrapper;

public class EntityMeta<E> {

    protected Class<E> clazz;

    protected final String table;

    protected final List<Field> fields;

    protected final Field idField;

    protected final Map<String, String> fieldColumnMapping;

    private final Set<String> columnNameSet;

    protected final E[] emptyArray;

    @SuppressWarnings("unchecked")
    public EntityMeta(Class<E> clazz) {
        this.clazz = clazz;
        this.table = getTable(clazz);
        this.fields = getAnnotatedFields(clazz);
        this.fieldColumnMapping = createFieldColumnMapping(fields);
        this.columnNameSet = new HashSet<>(fieldColumnMapping.values());
        this.idField = this.fields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElse(null);
        this.emptyArray = (E[]) Array.newInstance(clazz, 0);
    }

    public String getTable() {
        return table;
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<Field> getFieldsWithoutIdField() {
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (field == idField) {
                continue;
            }
            list.add(field);
        }
        return list;
    }

    public List<String> getColumnNamesByFields(List<Field> fields) {
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            columnNames.add(fieldColumnMapping.get(field.getName()));
        }
        return columnNames;
    }

    public String getColumnNameByField(Field field) {
        return getColumnNameByFieldName(field.getName());
    }

    public String getColumnNameByFieldName(String fieldName) {
        String columnName = fieldColumnMapping.get(fieldName);
        if (StringUtils.isBlank(columnName) && columnNameSet.contains(fieldName)) {
            return fieldName;
        }
        return columnName;
    }

    public E[] emptyArray() {
        return emptyArray;
    }

    public Field getIdField() {
        return idField;
    }

    public String getIdFieldName() {
        if (getIdField() == null) {
            return null;
        }
        return getIdField().getName();
    }

    public String getIdColumnName() {
        return fieldColumnMapping.get(getIdFieldName());
    }

    public List<String> getSelectStmts(StringWrapper columnWrapper) {
        List<String> list = new ArrayList<String>();
        for (Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            list.add(columnWrapper.wrap(entry.getValue()) + " AS " + entry.getKey());
        }
        return list;
    }

    private static String getTable(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null && StringUtils.isNotBlank(table.name())) {
            return table.name();
        }
        return camelToUnderscore(clazz.getSimpleName());
    }

    private static List<Field> getAnnotatedFields(Class<?> clazz) {
        List<Field> list = new ArrayList<Field>();
        if (clazz.getSuperclass() != Object.class) {
            list.addAll(getAnnotatedFields(clazz.getSuperclass()));
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }
            list.add(field);
        }
        return Collections.unmodifiableList(list);
    }

    private static Map<String, String> createFieldColumnMapping(List<Field> fields) {
        Map<String, String> mapping = new LinkedHashMap<String, String>();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            String columnName = StringUtils.isNotBlank(column.name())
                    ? column.name()
                    : camelToUnderscore(field.getName());
            mapping.put(field.getName(), columnName);
        }
        return Collections.unmodifiableMap(mapping);
    }

    private static String camelToUnderscore(String str) {
        return StringUtils.isBlank(str) ? "" : str.replaceAll("([^\\sA-Z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static <T> EntityMeta<T> newInstance(Class<T> clazz) {
        return new EntityMeta<T>(clazz);
    }

}
