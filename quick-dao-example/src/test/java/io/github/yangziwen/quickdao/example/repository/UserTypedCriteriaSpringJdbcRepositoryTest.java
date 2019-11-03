package io.github.yangziwen.quickdao.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.example.repository.base.BaseUserTypedCriteriaRepositoryTest;

public class UserTypedCriteriaSpringJdbcRepositoryTest extends BaseUserTypedCriteriaRepositoryTest {

    @Override
    protected UserSpringJdbcRepository createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

}
