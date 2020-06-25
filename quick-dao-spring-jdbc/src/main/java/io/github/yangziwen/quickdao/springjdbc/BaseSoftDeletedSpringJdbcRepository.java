package io.github.yangziwen.quickdao.springjdbc;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.springjdbc.BaseSpringJdbcRepository;

/**
 * BaseSoftDeletedSpringJdbcRepository
 * 基于逻辑删除的repository
 *
 * @author yangziwen
 *
 * @param <E>
 */
public abstract class BaseSoftDeletedSpringJdbcRepository<E> extends BaseSpringJdbcRepository<E> {

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

    /**
     * 逻辑删除的标识字段（不需要在entity中声明）
     * @return
     */
    protected abstract String getDeletedFlagColumn();

    /**
     * 已删除数据的逻辑删除标识字段值
     * @return
     */
    protected abstract Object getDeletedFlagValue();

    /**
     * 未删除数据的逻辑删除标识字段值
     * @return
     */
    protected abstract Object getNotDeletedFlagValue();

    /**
     * 数据表中的更新时间字段，返回空则逻辑删除时忽略更新时间
     * @return
     */
    protected abstract String getUpdateTimeColumn();

    /**
     * 数据表中更新时间字段的取值，返回空则逻辑删除时忽略更新时间
     * 只能返回new Date().getTime() 或 "now()"，不能返回Date对象
     * @return
     */
    protected abstract Object getUpdateTimeValue();

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
