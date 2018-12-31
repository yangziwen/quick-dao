package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import io.github.yangziwen.quickdao.core.SqlGenerator;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;

public class UserSql2oRepository extends BaseSql2oRepository<User> {

    public UserSql2oRepository(DataSource dataSource, SqlGenerator sqlGenerator) {
        super(dataSource, sqlGenerator);
    }

}
