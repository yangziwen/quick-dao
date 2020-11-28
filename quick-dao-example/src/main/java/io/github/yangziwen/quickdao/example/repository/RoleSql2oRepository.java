package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import io.github.yangziwen.quickdao.example.entity.Role;
import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;

public class RoleSql2oRepository extends BaseSql2oRepository<Role> {

    protected RoleSql2oRepository(DataSource dataSource) {
        super(dataSource);
    }

}
