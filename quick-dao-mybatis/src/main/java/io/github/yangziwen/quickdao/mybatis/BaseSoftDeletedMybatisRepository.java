package io.github.yangziwen.quickdao.mybatis;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.core.BaseSoftDeletedRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;

/**
 * BaseSoftDeletedMybatisRepository
 * 基于逻辑删除的repository
 *
 * @author yangziwen
 *
 * @param <E>
 */
public abstract class BaseSoftDeletedMybatisRepository<E> extends BaseMybatisRepository<E> implements BaseSoftDeletedRepository<E> {

    private final E emptyEntity;

    protected BaseSoftDeletedMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
        this.emptyEntity = newEntityInstance();
    }

    protected BaseSoftDeletedMybatisRepository(SqlSession sqlSession, SqlGenerator sqlGenerator) {
        super(sqlSession, sqlGenerator);
        this.emptyEntity = newEntityInstance();
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
    public int updateSelective(E entity, Criteria criteria) {
        criteria.and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        return super.updateSelective(entity, criteria);
    }

    /**
     * 按条件批量删除数据
     *
     * @param criteria      删除条件
     */
    @Override
    public int delete(Criteria criteria) {
        return delete(new Query().where(criteria));
    }

    /**
     * 逻辑删除
     */
    @Override
    public int delete(Query query) {

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

        Map<String, Object> paramMap = query.getCriteria().toParamMap();
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicUpdateStmt(sql, entityMeta.getClassType());
        return sqlSession.update(stmt, paramMap);
    }

    /**
     * 逻辑删除
     */
    @Override
    public int deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

    /**
     * 逻辑删除
     */
    @Override
    public int deleteById(Object id) {
        Criteria criteria = new Criteria()
                .and(entityMeta.getIdFieldName()).eq(id)
                .and(getDeletedFlagColumn()).eq(getNotDeletedFlagValue());
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, emptyEntity, criteria);
        String replacement = " SET " + sqlGenerator.getColumnWrapper().wrap(getDeletedFlagColumn()) + " = " + getDeletedFlagValue() + " ";
        if (StringUtils.isNotBlank(getUpdateTimeColumn()) && getUpdateTimeValue() != null) {
            replacement += ", " + sqlGenerator.getColumnWrapper().wrap(getUpdateTimeColumn()) + " = " + getUpdateTimeValue() + " ";
        }
        sql = sql.replaceFirst("\\sSET\\s", replacement);
        Map<String, Object> paramMap = criteria.toParamMap();
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicUpdateStmt(sql, entityMeta.getClassType());
        return sqlSession.update(stmt, paramMap);
    }

}
