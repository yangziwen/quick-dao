package io.github.yangziwen.quickdao.example.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.springjdbc.BaseSpringJdbcRepository;

public class UserSpringJdbcRepository extends BaseSpringJdbcRepository<User> {

    public UserSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<User> getUserListByUsernameStartWith(String prefix) {
        Criteria criteria = new Criteria().and("username").startWith(prefix);
        return list(new Query().where(criteria));
    }

}
