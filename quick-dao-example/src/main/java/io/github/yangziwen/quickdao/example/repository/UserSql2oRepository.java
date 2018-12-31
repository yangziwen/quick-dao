package io.github.yangziwen.quickdao.example.repository;

import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;

public class UserSql2oRepository extends BaseSql2oRepository<User> {

    public UserSql2oRepository(Sql2o sql2o) {
        super(sql2o);
    }

}
