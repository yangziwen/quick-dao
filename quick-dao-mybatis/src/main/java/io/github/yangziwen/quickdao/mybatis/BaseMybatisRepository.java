package io.github.yangziwen.quickdao.mybatis;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.EntityMeta;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.RepoKeys;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public abstract class BaseMybatisRepository<E> implements BaseRepository<E> {

    protected EntityMeta<E> entityMeta = EntityMeta
            .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));

    protected final SqlSession sqlSession;

    protected final SqlGenerator sqlGenerator;

    private final MappedStatementAssistant assistant;

    protected BaseMybatisRepository(SqlSession sqlSession) {
        this(sqlSession, new SqlGenerator(new StringWrapper("#{", "}")));
    }

    protected BaseMybatisRepository(SqlSession sqlSession, SqlGenerator sqlGenerator) {
        this.sqlSession = sqlSession;
        this.sqlGenerator = sqlGenerator;
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

    @Override
    public void insert(E entity) {
        String sql = sqlGenerator.generateInsertSql(entityMeta);
        String stmt = assistant.getDynamicInsertStmt(sql, entity.getClass(), entityMeta.getIdColumnName());
        sqlSession.insert(stmt, entity);
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
    public void update(E entity) {
        String sql = sqlGenerator.generateUpdateSql(entityMeta);
        String stmt = assistant.getDynamicUpdateStmt(sql, entity.getClass());
        sqlSession.update(stmt, entity);
    }

    @Override
    public void updateSelective(E entity) {
        String sql = sqlGenerator.generateUpdateSelectiveSql(entityMeta, entity);
        String stmt = assistant.getDynamicUpdateStmt(sql, entity.getClass());
        sqlSession.update(stmt, entity);
    }

    @Override
    public void deleteById(Object id) {
        String sql = sqlGenerator.generateDeleteByPrimaryKeySql(entityMeta);
        String stmt = assistant.getDynamicDeleteStmt(sql, id.getClass());
        sqlSession.delete(stmt, id);
    }

    @Override
    public void delete(Criteria criteria) {
        Map<String, Object> paramMap = criteria.toParamMap();
        String sql = sqlGenerator.generateDeleteByCriteriaSql(entityMeta, criteria);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        String stmt = assistant.getDynamicDeleteStmt(sql, criteria.getClass());
        sqlSession.delete(stmt, paramMap);
    }

    @Override
    public void deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        delete(new Criteria().and(entityMeta.getIdFieldName()).in(ids));
    }

}
