package io.github.yangziwen.quickdao.sql2o;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.BaseSoftDeletedRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;

/**
 * BaseSoftDeletedSql2oRepository
 * 基于逻辑删除的repository
 *
 * @author yangziwen
 *
 * @param <E>
 */
public abstract class BaseSoftDeletedSql2oRepository<E> extends BaseSql2oRepository<E> implements BaseSoftDeletedRepository<E> {

    private final E emptyEntity;

    protected BaseSoftDeletedSql2oRepository(Sql2o sql2o) {
        super(sql2o);
        this.emptyEntity = newEntityInstance();
     }

    protected BaseSoftDeletedSql2oRepository(Sql2o sql2o, SqlGenerator sqlGenerator) {
         super(sql2o, sqlGenerator);
         this.emptyEntity = newEntityInstance();
     }

    protected BaseSoftDeletedSql2oRepository(DataSource dataSource) {
         this(new Sql2o(dataSource));
     }

    protected BaseSoftDeletedSql2oRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
         this(new Sql2o(dataSource), sqlGenerator);
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
        return first(new Query().where(new Criteria().and(entityMeta.getIdFieldName()).eq(id)
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
     * @param criteria 删除条件
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

        String replacement = " SET " + sqlGenerator.getColumnWrapper().wrap(getDeletedFlagColumn()) + " = "
                + getDeletedFlagValue() + " ";

        if (StringUtils.isNotBlank(getUpdateTimeColumn()) && getUpdateTimeValue() != null) {
            replacement += ", " + sqlGenerator.getColumnWrapper().wrap(getUpdateTimeColumn()) + " = "
                    + getUpdateTimeValue() + " ";
        }

        sql = sql.replaceFirst("\\sSET\\s", replacement);

        if (query.getLimit() > 0) {
            sql += " LIMIT " + query.getLimit();
        }

        Map<String, Object> paramMap = query.getCriteria().toParamMap();

        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);

        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            sql2oQuery.executeUpdate();
        }
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
        String replacement = " SET " + sqlGenerator.getColumnWrapper().wrap(getDeletedFlagColumn()) + " = "
                + getDeletedFlagValue() + " ";
        if (StringUtils.isNotBlank(getUpdateTimeColumn()) && getUpdateTimeValue() != null) {
            replacement += ", " + sqlGenerator.getColumnWrapper().wrap(getUpdateTimeColumn()) + " = "
                    + getUpdateTimeValue() + " ";
        }
        sql = sql.replaceFirst("\\sSET\\s", replacement);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Entry<String, Object> entry : criteria.toParamMap().entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            sql2oQuery.executeUpdate();
        }
    }

}
