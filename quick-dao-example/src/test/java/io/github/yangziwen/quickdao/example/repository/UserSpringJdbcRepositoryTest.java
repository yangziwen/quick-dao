package io.github.yangziwen.quickdao.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;

public class UserSpringJdbcRepositoryTest extends BaseUserRepositoryTest {

    @Override
    protected BaseRepository<User> createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

}
