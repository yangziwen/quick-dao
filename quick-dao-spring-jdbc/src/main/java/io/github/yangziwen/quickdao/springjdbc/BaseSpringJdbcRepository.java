package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.EntityMeta;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public abstract class BaseSpringJdbcRepository<E> implements BaseRepository<E> {

    protected EntityMeta<E> entityMeta = EntityMeta
            .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));

    protected final SqlGenerator sqlGenerator;

    protected final NamedParameterJdbcTemplate jdbcTemplate;

    protected final SimpleJdbcInsert jdbcInsert;

    protected final RowMapper<E> rowMapper;

    protected BaseSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new SqlGenerator(new StringWrapper(":", "")));
    }

    protected BaseSpringJdbcRepository(JdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(entityMeta.getTable())
                .usingColumns(entityMeta.getColumnNamesWithoutIdColumn().stream()
                        .map(sqlGenerator.getColumnWrapper()::wrap)
                        .collect(Collectors.toList())
                        .toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                .usingGeneratedKeyColumns(entityMeta.getIdColumnName());
        this.rowMapper = createRowMapper(entityMeta.getClassType());
    }

    @Override
    public E getById(Object id) {
        return first(new Query().where(new Criteria().and(entityMeta.getIdFieldName()).eq(id)));
    }

    @Override
    public List<E> list(Query query) {
        String sql = sqlGenerator.generateListByQuerySql(entityMeta, query);
        return jdbcTemplate.query(sql, query.toParamMap(), new RowMapperResultSetExtractor<E>(rowMapper));
    }

    @Override
    public List<E> listByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return list(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    @Override
    public Integer count(Query query) {
        String sql = sqlGenerator.generateCountByQuerySql(entityMeta, query);
        return jdbcTemplate.queryForObject(sql, query.toParamMap(), Integer.class);
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
    public void deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    private SqlParameterSource createSqlParameterSource(E entity) {
        return new BeanPropertySqlParameterSource(entity, sqlGenerator.getColumnWrapper());
    }

    protected List<SqlParameterSource> createBatchSqlParameterSource(List<E> entities) {
        return entities.stream()
                .map(this::createSqlParameterSource)
                .collect(Collectors.toList());
    }

    protected RowMapper<E> createRowMapper(Class<E> entityClass) {
        BeanPropertyRowMapper<E> rowMapper = new BeanPropertyRowMapper<E>() {
            @Override
            protected void initBeanWrapper(BeanWrapper bw) {
                super.initBeanWrapper(bw);
            }
        };
        rowMapper.setMappedClass(entityClass);
        return rowMapper;
    }

}
