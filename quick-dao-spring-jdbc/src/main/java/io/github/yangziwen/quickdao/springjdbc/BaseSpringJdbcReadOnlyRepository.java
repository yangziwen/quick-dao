package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import io.github.yangziwen.quickdao.core.BaseCommonRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public abstract class BaseSpringJdbcReadOnlyRepository<E> extends BaseCommonRepository<E> {

    private static final int DEFAULT_SQL_CACHE_LIMIT = 32;

    protected final NamedParameterJdbcTemplate jdbcTemplate;

    protected final RowMapper<E> rowMapper;

    protected BaseSpringJdbcReadOnlyRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new SqlGenerator(new StringWrapper(":", "")));
    }

    protected BaseSpringJdbcReadOnlyRepository(JdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
        super(sqlGenerator);
        this.jdbcTemplate = createNamedParameterJdbcTemplate(jdbcTemplate);
        this.rowMapper = createRowMapper(entityMeta.getClassType());
    }

    @Override
    public E getById(Object id) {
        return first(new Query().where(new Criteria().and(entityMeta.getIdFieldName()).eq(id)));
    }

    @Override
    public List<E> list(Query query) {
        String sql = sqlGenerator.generateListByQuerySql(entityMeta, query);
        return jdbcTemplate.query(sql, query.toParamMap(), rowMapper);
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

    protected RowMapper<E> createRowMapper(Class<E> entityClass) {
        return new BeanPropertyRowMapper<>(entityClass);
    }

    protected NamedParameterJdbcTemplate createNamedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        template.setCacheLimit(getSqlCacheLimit());
        return template;
    }

    protected int getSqlCacheLimit() {
        return DEFAULT_SQL_CACHE_LIMIT;
    }

}
