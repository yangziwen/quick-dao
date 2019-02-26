package io.github.yangziwen.quickdao.example.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.example.entity.User;

public class UserSpringJdbcRepositoryTest extends BaseUserRepositoryTest {

    @Override
    protected UserSpringJdbcRepository createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

    @Test
    public void testGetUserListByUsernameStartWith() {
        List<User> users = createRepository().getUserListByUsernameStartWith("user");
        Assert.assertEquals(2, users.size());
    }

}
