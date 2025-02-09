package io.github.yangziwen.quickdao.sql2o;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.RepoKeys;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public abstract class BaseSql2oRepository<E> extends BaseSql2oReadOnlyRepository<E> implements BaseRepository<E> {

    protected BaseSql2oRepository(Sql2o sql2o) {
       super(sql2o);
    }

    protected BaseSql2oRepository(Sql2o sql2o, SqlGenerator sqlGenerator) {
        super(sql2o, sqlGenerator);
    }

    protected BaseSql2oRepository(DataSource dataSource) {
        this(new Sql2o(dataSource));
    }

    protected BaseSql2oRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
        this(new Sql2o(dataSource), sqlGenerator);
    }

    @Override
    public int insert(E entity) {
        String sql = sqlGenerator.generateInsertSql(entityMeta);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            List<Field> fields = entityMeta.getIdGeneratedValue() != null
                    ? entityMeta.getFieldsWithoutIdField()
                    : entityMeta.getFields();
            for (Field field : fields) {
                Object value = ReflectionUtil.getFieldValue(entity, field);
                sql2oQuery.addParameter(field.getName(), value);
            }
            Connection connection = sql2oQuery.executeUpdate();
            Object id = connection.getKey();
            if (entityMeta.getIdGeneratedValue() != null) {
                entityMeta.fillIdValue(entity, id);
            }
            return connection.getResult();
        }
    }

    @Override
    public int batchInsert(List<E> entities, int batchSize) {
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }
        int size = 0;
        String sql = "";
        int affectedRows = 0;
        for (int i = 0; i < entities.size(); i += batchSize) {
            List<E> subList = entities.subList(i, Math.min(i + batchSize, entities.size()));
            if (size != subList.size() || StringUtils.isBlank(sql)) {
                sql = sqlGenerator.generateBatchInsertSql(entityMeta, subList.size());
            }
            affectedRows += doBatchInsert(subList, sql);
        }
        return affectedRows;
    }

    private int doBatchInsert(List<E> entities, String sql) {
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            List<Field> fields = entityMeta.getIdGeneratedValue() != null
                    ? entityMeta.getFieldsWithoutIdField()
                    : entityMeta.getFields();
            for (int i = 0; i < entities.size(); i++) {
                E entity = entities.get(i);
                for (Field field : fields) {
                    Object value = ReflectionUtil.getFieldValue(entity, field);
                    sql2oQuery.addParameter(field.getName() + RepoKeys.__ + i, value);
                }
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int update(E entity) {
        String sql = sqlGenerator.generateUpdateSql(entityMeta);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Field field : entityMeta.getFields()) {
                Object value = ReflectionUtil.getFieldValue(entity, field);
                sql2oQuery.addParameter(field.getName(), value);
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int updateSelective(E entity) {
        String sql = sqlGenerator.generateUpdateSelectiveSql(entityMeta, entity);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Field field : entityMeta.getFields()) {
                Object value = ReflectionUtil.getFieldValue(entity, field);
                if (value == null) {
                    continue;
                }
                sql2oQuery.addParameter(field.getName(), value);
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int updateSelective(E entity, Criteria criteria) {
        Map<String, Object> paramMap = criteria.toParamMap();
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, entity, criteria);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Field field : entityMeta.getFields()) {
                Object value = ReflectionUtil.getFieldValue(entity, field);
                if (value == null) {
                    continue;
                }
                sql2oQuery.addParameter(field.getName(), value);
            }
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int deleteById(Object id) {
        String sql = sqlGenerator.generateDeleteByPrimaryKeySql(entityMeta);
        Field idField = entityMeta.getIdField();
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            return sql2oQuery
                .addParameter(idField.getName(), id)
                .executeUpdate()
                .getResult();
        }
    }

    @Override
    public int delete(Criteria criteria) {
        String sql = sqlGenerator.generateDeleteByCriteriaSql(entityMeta, criteria);
        Map<String, Object> paramMap = criteria.toParamMap();
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int delete(Query query) {
        String sql = sqlGenerator.generateDeleteByQuerySql(entityMeta, query);
        Map<String, Object> paramMap = query.toParamMap();
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            return sql2oQuery.executeUpdate().getResult();
        }
    }

    @Override
    public int deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

}
