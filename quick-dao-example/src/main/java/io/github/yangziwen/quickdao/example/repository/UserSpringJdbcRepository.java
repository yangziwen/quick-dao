package io.github.yangziwen.quickdao.example.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;
import io.github.yangziwen.quickdao.springjdbc.BaseSpringJdbcRepository;

public class UserSpringJdbcRepository extends BaseSpringJdbcRepository<User> {

    public UserSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<User> getUserListByUsernameStartWith(String prefix) {
        Criteria criteria = new Criteria().and("username").startWith(prefix);
        return list(new Query().where(criteria));
    }

    public List<String> listUsernameOfEldestMaleUser(int limit) {
        Query query = newTypedQuery()
                .select(User::getUsername)
                .where(criteria -> criteria
                        .and(User::getGender).eq(Gender.MALE))
                .orderBy(User::getGender, Direction.DESC)
                .limit(limit);
        return list(query).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

}
