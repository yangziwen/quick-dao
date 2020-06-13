package io.github.yangziwen.quickdao.sql2o;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.Convert;

import io.github.yangziwen.quickdao.core.BaseCommonRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.RepoKeys;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.StringWrapper;

public abstract class BaseSql2oReadOnlyRepository<E> extends BaseCommonRepository<E> {

    static {
        Convert.registerEnumConverter(new CustomEnumConverterFactory());
    }

    protected final Sql2o sql2o;

    protected BaseSql2oReadOnlyRepository(Sql2o sql2o) {
        this(sql2o, new SqlGenerator(new StringWrapper(":", "")));
    }

    protected BaseSql2oReadOnlyRepository(Sql2o sql2o, SqlGenerator sqlGenerator) {
        super(sqlGenerator);
        this.sql2o = sql2o;
    }

    protected BaseSql2oReadOnlyRepository(DataSource dataSource) {
        this(new Sql2o(dataSource));
    }

    protected BaseSql2oReadOnlyRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
        this(new Sql2o(dataSource), sqlGenerator);
    }

    @Override
    public E getById(Object id) {
        return first(new Query().where(new Criteria().and(entityMeta.getIdFieldName()).eq(id)));
    }

    @Override
    public List<E> list(Query query) {
        String sql = sqlGenerator.generateListByQuerySql(entityMeta, query);
        Map<String, Object> paramMap = query.toParamMap();
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        try (Connection conn = sql2o.open();
                org.sql2o.Query sql2oQuery = conn.createQuery(sql)) {
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            return sql2oQuery.executeAndFetch(entityMeta.getClassType());
        }
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
        Map<String, Object> paramMap = query.toParamMap();
        paramMap.remove(RepoKeys.OFFSET);
        paramMap.remove(RepoKeys.LIMIT);
        sql = sqlGenerator.flattenCollectionValues(sql, paramMap);
        try (Connection conn = sql2o.open()) {
            org.sql2o.Query sql2oQuery = conn.createQuery(sql);
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                sql2oQuery.addParameter(entry.getKey(), entry.getValue());
            }
            return sql2oQuery.executeScalar(Integer.class);
        }
    }

}
