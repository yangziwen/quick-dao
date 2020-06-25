package io.github.yangziwen.quickdao.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserSoftDeletedRepositoryTest;

public class UserSoftDeletedSpringJdbcRepositoryTest extends BaseUserSoftDeletedRepositoryTest {

    @Override
    protected BaseRepository<User> createSoftDeletedRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSoftDeletedSpringJdbcRepository(template);
    }

    @Override
    protected BaseRepository<User> createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

}
