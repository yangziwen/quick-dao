package io.github.yangziwen.quickdao.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.springjdbc.BaseSpringJdbcRepository;

public class UserSpringJdbcRepository extends BaseSpringJdbcRepository<User> {

    public UserSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

}
