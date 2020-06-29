package io.github.yangziwen.quickdao.mybatis;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.RepoKeys;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;

public abstract class BaseMybatisRepository<E> extends BaseMybatisReadOnlyRepository<E> implements BaseRepository<E> {

    protected BaseMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
    }

    protected BaseMybatisRepository(SqlSession sqlSession, SqlGenerator sqlGenerator) {
        super(sqlSession, sqlGenerator);
    }

    @Override
    public int insert(E entity) {
        String sql = sqlGenerator.generateInsertSql(entityMeta);
        String stmt = assistant.getDynamicInsertStmt(sql, entity.getClass(), entityMeta.getIdColumnName());
        return sqlSession.insert(stmt, entity);
    }

    @Override
    public void batchInsert(List<E> entities, int batchSize) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        int size = 0;
        String sql = "";
        for (int i = 0; i < entities.size(); i+= batchSize) {
            List<E> subList = entities.subList(i, Math.min(i + batchSize, entities.size()));
            if (size != subList.size() || StringUtils.isBlank(sql)) {
                sql = sqlGenerator.generateBatchInsertSql(entityMeta, subList.size());
            }
            String stmt = assistant.getDynamicInsertStmt(sql, entities.getClass(), null);
            doBatchInsert(subList, stmt);
        }
    }

    private void doBatchInsert(List<E> entities, String stmt) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        List<Field> fields = entityMeta.getFieldsWithoutIdField();
        for (int i = 0; i < entities.size(); i++) {
            E entity = entities.get(i);
            for (Field field : fields) {
                Object value = ReflectionUtil.getFieldValue(entity, field);
                paramMap.put(field.getName() + RepoKeys.__ + i, value);
            }
        }
        sqlSession.insert(stmt, paramMap);
    }

    @Override
    public int update(E entity) {
        String sql = sqlGenerator.generateUpdateSql(entityMeta);
        String stmt = assistant.getDynamicUpdateStmt(sql, entity.getClass());
        return sqlSession.update(stmt, entity);
    }

    @Override
    public int updateSelective(E entity) {
        String sql = sqlGenerator.generateUpdateSelectiveSql(entityMeta, entity);
        String stmt = assistant.getDynamicUpdateStmt(sql, entity.getClass());
        return sqlSession.update(stmt, entity);
    }

    @Override
    public int updateSelective(E entity, Criteria criteria) {
        Map<String, Object> paramMap = criteria.toParamMap();
        String sql = sqlGenerator.generateUpdateSelectiveByCriteriaSql(entityMeta, entity, criteria);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicUpdateStmt(sql, entity.getClass());
        Map<String, Object> compositeParamMap = new HashMap<>(paramMap);
        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            Object value = ReflectionUtil.getFieldValue(entity, field);
            if (value == null) {
                continue;
            }
            compositeParamMap.put(field.getName(), value);
        }
        return sqlSession.update(stmt, compositeParamMap);
    }

    @Override
    public int deleteById(Object id) {
        String sql = sqlGenerator.generateDeleteByPrimaryKeySql(entityMeta);
        String stmt = assistant.getDynamicDeleteStmt(sql, id.getClass());
        return sqlSession.delete(stmt, id);
    }

    @Override
    public int delete(Criteria criteria) {
        Map<String, Object> paramMap = criteria.toParamMap();
        String sql = sqlGenerator.generateDeleteByCriteriaSql(entityMeta, criteria);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicDeleteStmt(sql, criteria.getClass());
        return sqlSession.delete(stmt, paramMap);
    }

    @Override
    public int delete(Query query) {
        Map<String, Object> paramMap = query.toParamMap();
        String sql = sqlGenerator.generateDeleteByQuerySql(entityMeta, query);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicDeleteStmt(sql, query.getClass());
        return sqlSession.delete(stmt, paramMap);
    }

    @Override
    public int deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

}
