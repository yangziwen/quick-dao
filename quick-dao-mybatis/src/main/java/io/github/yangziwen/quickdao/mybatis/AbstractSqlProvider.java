package io.github.yangziwen.quickdao.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.github.yangziwen.quickdao.core.EntityMeta;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;
import io.github.yangziwen.quickdao.core.util.StringWrapper;
import lombok.Getter;
import lombok.Setter;

public class AbstractSqlProvider<E> {

    private EntityMeta<E> entityMeta = EntityMeta
            .newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));

    @Getter
    @Setter
    private SqlGenerator generator = new SqlGenerator(new StringWrapper("#{", "}"));

    public String getById(Object id) {
        return generator.generateGetByPrimaryKeySql(entityMeta);
    }

    public String list(Query query) {
        query.toParamMap();
        return generator.generateListByQuerySql(entityMeta, query);
    }

    public String count(Query query) {
        query.toParamMap();
        return generator.generateCountByQuerySql(entityMeta, query);
    }

    public String delete(Query query) {
        query.asMap().putAll(query.getCriteria().toParamMap());
        return generator.generateDeleteByCriteriaSql(entityMeta, query.getCriteria());
    }

    public String deleteById(Object id) {
        return generator.generateDeleteByPrimaryKeySql(entityMeta);
    }

    public String insert(E entity) {
        return generator.generateInsertSql(entityMeta);
    }

    @Deprecated
    public String batchInsert(@Param("list") List<E> entities) {
        return generator.generateBatchInsertSql(entityMeta, entities.size());
    }

    public String update(E entity) {
        return generator.generateUpdateSql(entityMeta);
    }

    public String updateSelective(E entity) {
        return generator.generateUpdateSelectiveSql(entityMeta, entity);
    }

}
