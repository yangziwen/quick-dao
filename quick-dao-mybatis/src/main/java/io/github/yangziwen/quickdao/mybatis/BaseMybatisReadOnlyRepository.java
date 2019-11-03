package io.github.yangziwen.quickdao.mybatis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.core.BaseCommonRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public abstract class BaseMybatisReadOnlyRepository<E> extends BaseCommonRepository<E> {

    protected final SqlSession sqlSession;

    protected final MappedStatementAssistant assistant;

    protected BaseMybatisReadOnlyRepository(SqlSession sqlSession) {
        this(sqlSession, new SqlGenerator(new StringWrapper("#{", "}")));
    }

    protected BaseMybatisReadOnlyRepository(SqlSession sqlSession, SqlGenerator sqlGenerator) {
        super(sqlGenerator);
        this.sqlSession = sqlSession;
        this.assistant = new MappedStatementAssistant(sqlSession.getConfiguration());
    }

    @Override
    public E getById(Object id) {
        return first(new Query().where(new Criteria().and(entityMeta.getIdFieldName()).eq(id)));
    }

    @Override
    public List<E> list(Query query) {
        Map<String, Object> paramMap = query.toParamMap();
        String sql = sqlGenerator.generateListByQuerySql(entityMeta, query);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicSelectStmt(sql, query.getClass(), entityMeta.getClassType());
        return sqlSession.selectList(stmt, paramMap);
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
        Map<String, Object> paramMap = query.toParamMap();
        String sql = sqlGenerator.generateCountByQuerySql(entityMeta, query);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicSelectStmt(sql, query.getClass(), Integer.class);
        return sqlSession.selectOne(stmt, paramMap);
    }

}
