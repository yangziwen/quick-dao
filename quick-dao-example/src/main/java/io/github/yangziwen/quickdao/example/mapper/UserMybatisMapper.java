package io.github.yangziwen.quickdao.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.mybatis.AbstractSqlProvider;

public interface UserMybatisMapper {

    class SqlProvider extends AbstractSqlProvider<User> { }

    @SelectProvider(type = SqlProvider.class, method = "getById")
    User getById(Object id);

    @SelectProvider(type = SqlProvider.class, method = "list")
    List<User> list(Query query);

    @SelectProvider(type = SqlProvider.class, method = "count")
    Integer count(Query query);

    @InsertProvider(type = SqlProvider.class, method="insert")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @InsertProvider(type = SqlProvider.class, method="batchInsert")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void batchInsert(List<User> users);

    @UpdateProvider(type = SqlProvider.class, method="update")
    void update(User user);

    @UpdateProvider(type = SqlProvider.class, method = "updateSelective")
    void updateSelective(User user);

    @DeleteProvider(type = SqlProvider.class, method = "deleteById")
    void deleteById(Object id);

    @DeleteProvider(type = SqlProvider.class, method = "delete")
    void delete(Query query);

}
