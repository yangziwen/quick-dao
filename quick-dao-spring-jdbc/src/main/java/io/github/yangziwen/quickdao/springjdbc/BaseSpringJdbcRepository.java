package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
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

    protected BaseSpringJdbcRepository(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    protected BaseSpringJdbcRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
        this(new JdbcTemplate(dataSource), sqlGenerator);
    }

    private SimpleJdbcInsert createJdbcInsert(JdbcTemplate jdbcTemplate) {
        List<String> columns = entityMeta.getIdGeneratedValue() != null
                ? entityMeta.getColumnNamesWithoutIdColumn()
                : entityMeta.getColumnNames();
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(entityMeta.getTable())
                .usingColumns(columns.stream()
                        .map(sqlGenerator.getColumnWrapper()::wrap)
                        .collect(Collectors.toList())
                        .toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        if (entityMeta.getIdGeneratedValue() != null) {
            jdbcInsert.usingGeneratedKeyColumns(entityMeta.getIdColumnName());
        }
        return jdbcInsert;
    }

    @Override
    public int insert(E entity) {
        if (entityMeta.getIdGeneratedValue() != null) {
            Number key = jdbcInsert.executeAndReturnKey(createSqlParameterSource(entity));
            entityMeta.fillIdValue(entity, key);
            return key != null ? 1 : 0;
        } else {
            return jdbcInsert.execute(createSqlParameterSource(entity));
        }
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
    public int update(E entity) {
        String sql = sqlGenerator.generateUpdateSql(entityMeta);
        return jdbcTemplate.update(sql, createSqlParameterSource(entity));
    }

    @Override
    public int updateSelective(E entity) {
        String sql = sqlGenerator.generateUpdateSelectiveSql(entityMeta, entity);
        return jdbcTemplate.update(sql, createSqlParameterSource(entity));
    }

    @Override
    public int updateSelective(E entity, Criteria criteria) {
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, entity, criteria);
        SqlParameterSource source = new CompositeSqlParameterSource(
                createSqlParameterSource(entity),
                createSqlParameterSource(criteria));
        return jdbcTemplate.update(sql, source);
    }

    @Override
    public int deleteById(Object id) {
        String sql = sqlGenerator.generateDeleteByPrimaryKeySql(entityMeta);
        return jdbcTemplate.update(sql, new MapSqlParameterSource().addValue(entityMeta.getIdColumnName(), id));
    }

    @Override
    public int delete(Criteria criteria) {
        String sql = sqlGenerator.generateDeleteByCriteriaSql(entityMeta, criteria);
        return jdbcTemplate.update(sql, createSqlParameterSource(criteria));
    }

    @Override
    public int delete(Query query) {
        String sql = sqlGenerator.generateDeleteByQuerySql(entityMeta, query);
        return jdbcTemplate.update(sql, createSqlParameterSource(query.toParamMap()));
    }

    @Override
    public int deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    protected SqlParameterSource createSqlParameterSource(Criteria criteria) {
        return createSqlParameterSource(criteria.toParamMap());
    }

    protected SqlParameterSource createSqlParameterSource(E entity) {
        return new BeanPropertySqlParameterSource(entity, sqlGenerator.getColumnWrapper());
    }

    protected List<SqlParameterSource> createBatchSqlParameterSource(List<E> entities) {
        return entities.stream()
                .map(this::createSqlParameterSource)
                .collect(Collectors.toList());
    }

}
