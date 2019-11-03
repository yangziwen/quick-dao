package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;

public abstract class BaseSpringJdbcRepository<E> extends BaseSpringJdbcReadOnlyRepository<E> implements BaseRepository<E> {

    protected final SimpleJdbcInsert jdbcInsert;

    protected BaseSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcInsert = createJdbcInsert(jdbcTemplate);
    }

    protected BaseSpringJdbcRepository(JdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
        super(jdbcTemplate, sqlGenerator);
        this.jdbcInsert = createJdbcInsert(jdbcTemplate);
    }

    private SimpleJdbcInsert createJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(entityMeta.getTable())
                .usingColumns(entityMeta.getColumnNamesWithoutIdColumn().stream()
                        .map(sqlGenerator.getColumnWrapper()::wrap)
                        .collect(Collectors.toList())
                        .toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                .usingGeneratedKeyColumns(entityMeta.getIdColumnName());
    }

    @Override
    public void insert(E entity) {
        Number key = jdbcInsert.executeAndReturnKey(createSqlParameterSource(entity));
        entityMeta.fillIdValue(entity, key);
    }

    @Override
    public void batchInsert(List<E> entities, int batchSize) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        for (int i = 0, l = entities.size(); i < l; i += batchSize) {
            List<SqlParameterSource> paramSourceList = createBatchSqlParameterSource(
                    entities.subList(i, Math.min(i + batchSize, l)));
            jdbcInsert.executeBatch(paramSourceList.toArray(new SqlParameterSource[] {}));
        }
    }

    @Override
    public void update(E entity) {
        String sql = sqlGenerator.generateUpdateSql(entityMeta);
        jdbcTemplate.update(sql, createSqlParameterSource(entity));
    }

    @Override
    public void updateSelective(E entity) {
        String sql = sqlGenerator.generateUpdateSelectiveSql(entityMeta, entity);
        jdbcTemplate.update(sql, createSqlParameterSource(entity));
    }

    public void updateSelective(E entity, Criteria criteria) {
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, entity, criteria);
        SqlParameterSource source = new CompositeSqlParameterSource(
                createSqlParameterSource(entity),
                createSqlParameterSource(criteria));
        jdbcTemplate.update(sql, source);
    }

    @Override
    public void deleteById(Object id) {
        String sql = sqlGenerator.generateDeleteByPrimaryKeySql(entityMeta);
        jdbcTemplate.update(sql, new MapSqlParameterSource().addValue(entityMeta.getIdColumnName(), id));
    }

    @Override
    public void delete(Criteria criteria) {
        String sql = sqlGenerator.generateDeleteByCriteriaSql(entityMeta, criteria);
        jdbcTemplate.update(sql, new Query().where(criteria).toParamMap());
    }

    @Override
    public void delete(Query query) {
        String sql = sqlGenerator.generateDeleteByQuerySql(entityMeta, query);
        jdbcTemplate.update(sql, query.toParamMap());
    }

    @Override
    public void deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    private SqlParameterSource createSqlParameterSource(Criteria criteria) {
        return new MapSqlParameterSource(criteria.toParamMap());
    }

    private SqlParameterSource createSqlParameterSource(E entity) {
        return new BeanPropertySqlParameterSource(entity, sqlGenerator.getColumnWrapper());
    }

    protected List<SqlParameterSource> createBatchSqlParameterSource(List<E> entities) {
        return entities.stream()
                .map(this::createSqlParameterSource)
                .collect(Collectors.toList());
    }

}
