package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import io.github.yangziwen.quickdao.example.entity.Role;
import io.github.yangziwen.quickdao.springjdbc.BaseSpringJdbcRepository;

public class RoleSpringJdbcRepository extends BaseSpringJdbcRepository<Role> {

    public RoleSpringJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 按用户名查询角色
     * @param username
     * @return
     */
    public Role getRoleByUsername(String username) {
        return firstCriteria(criteria -> criteria
                .and(Role::getUsername).eq(username));
    }

}
