package io.github.yangziwen.quickdao.example.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.example.entity.User;

public class UserSql2oRepositoryTest extends BaseUserRepositoryTest {

    @Override
    protected UserSql2oRepository createRepository() {
        Sql2o sql2o = new Sql2o(dataSource);
        return new UserSql2oRepository(sql2o);
    }

    @Test
    public void testGetUserListByUsernames() {
        List<User> users = createRepository().getUserListByUsernames("user1", "user3");
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("user1", users.get(0).getUsername());
    }

    @Test
    public void testGetUserListByUsernamesWithoutArg() {
        List<User> users = createRepository().getUserListByUsernames();
        Assert.assertEquals(0, users.size());
    }

}
