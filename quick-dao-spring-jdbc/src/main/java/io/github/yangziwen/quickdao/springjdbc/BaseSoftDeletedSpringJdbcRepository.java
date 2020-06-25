package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.BaseSoftDeletedRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;

/**
 * BaseSoftDeletedSpringJdbcRepository
 * 基于逻辑删除的repository
 *
 * @author yangziwen
 *
 * @param <E>
 */
public abstract class BaseSoftDeletedSpringJdbcRepository<E> extends BaseSpringJdbcRepository<E> implements BaseSoftDeletedRepository<E> {

    private final E emptyEntity;

    protected BaseSoftDeletedSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.emptyEntity = newEntityInstance();
    }

    protected BaseSoftDeletedSpringJdbcRepository(JdbcTemplate jdbcTemplate, SqlGenerator sqlGenerator) {
        super(jdbcTemplate, sqlGenerator);
        this.emptyEntity = newEntityInstance();
    }

    protected BaseSoftDeletedSpringJdbcRepository(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    protected BaseSoftDeletedSpringJdbcRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
        this(new JdbcTemplate(dataSource), sqlGenerator);
    }

    private E newEntityInstance() {
        try {
            return entityMeta.getClassType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public E getById(Object id) {
        return first(new Query().where(new Criteria()
                .and(entityMeta.getIdFieldName()).eq(id)
                .and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue())));
    }

    @Override
    public List<E> list(Query query) {
        query.getCriteria().and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        return super.list(query);
    }

    @Override
    public Integer count(Query query) {
        query.getCriteria().and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        return super.count(query);
    }

    @Override
    public void updateSelective(E entity, Criteria criteria) {
        criteria.and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        super.updateSelective(entity, criteria);
    }

    /**
     * 按条件批量删除数据
     *
     * @param criteria      删除条件
     */
    @Override
    public void delete(Criteria criteria) {
        delete(new Query().where(criteria));
    }

    /**
     * 逻辑删除
     */
    @Override
    public void delete(Query query) {

        query.getCriteria().and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());

        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, emptyEntity, query.getCriteria());

        String replacement = " SET " + sqlGenerator.getColumnWrapper().wrap(getDeletedFlagColumn()) + " = " + getDeletedFlagValue() + " ";

        if (StringUtils.isNotBlank(getUpdateTimeColumn()) && getUpdateTimeValue() != null) {
            replacement += ", " + sqlGenerator.getColumnWrapper().wrap(getUpdateTimeColumn()) + " = " + getUpdateTimeValue() + " ";
        }

        sql = sql.replaceFirst("\\sSET\\s", replacement);

        if (query.getLimit() > 0) {
            sql += " LIMIT " + query.getLimit();
        }

        jdbcTemplate.update(sql, createSqlParameterSource(query.toParamMap()));
    }

    /**
     * 逻辑删除
     */
    @Override
    public void deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    /**
     * 逻辑删除
     */
    @Override
    public void deleteById(Object id) {
        Criteria criteria = new Criteria()
                .and(entityMeta.getIdFieldName()).eq(id)
                .and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, emptyEntity, criteria);
        String replacement = " SET " + sqlGenerator.getColumnWrapper().wrap(getDeletedFlagColumn()) + " = " + getDeletedFlagValue() + " ";
        if (StringUtils.isNotBlank(getUpdateTimeColumn()) && getUpdateTimeValue() != null) {
            replacement += ", " + sqlGenerator.getColumnWrapper().wrap(getUpdateTimeColumn()) + " = " + getUpdateTimeValue() + " ";
        }
        sql = sql.replaceFirst("\\sSET\\s", replacement);
        jdbcTemplate.update(sql, createSqlParameterSource(criteria));
    }

}
