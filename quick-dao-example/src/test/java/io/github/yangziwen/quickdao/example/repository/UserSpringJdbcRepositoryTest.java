package io.github.yangziwen.quickdao.example.repository;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserRepositoryTest;

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

    @Test
    public void testUpdateSelectiveWithCriteria() {
        Long id = 1L;
        String username = "user1_modified";
        User user = User.builder()
                .username(username)
                .build();
        Criteria criteria = new Criteria().and("id").in(Arrays.asList(id));
        UserSpringJdbcRepository repository = createRepository();
        repository.updateSelective(user, criteria);
        Assert.assertEquals(username, repository.getById(id).getUsername());
    }

    @Test
    public void testListUsernameOfEldestMaleUser() {
        // 数据集里 user1 为 MALE(1)、user2 为 FEMALE(2)
        Assert.assertEquals(Arrays.asList("user1"), createRepository().listUsernameOfEldestMaleUser(10));
        Assert.assertEquals(Arrays.asList("user1"), createRepository().listUsernameOfEldestMaleUser(1));
    }

}
