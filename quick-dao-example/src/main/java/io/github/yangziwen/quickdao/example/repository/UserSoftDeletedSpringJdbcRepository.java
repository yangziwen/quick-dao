package io.github.yangziwen.quickdao.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.springjdbc.BaseSoftDeletedSpringJdbcRepository;

public class UserSoftDeletedSpringJdbcRepository extends BaseSoftDeletedSpringJdbcRepository<User> {

    public UserSoftDeletedSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public String getDeletedFlagColumn() {
        return "valid";
    }

    @Override
    public Object getDeletedFlagValue() {
        return false;
    }

    @Override
    public Object getNotDeletedFlagValue() {
        return true;
    }

    @Override
    public String getUpdateTimeColumn() {
        return "update_time";
    }

    @Override
    public Object getUpdateTimeValue() {
        return "now()";
    }

}
