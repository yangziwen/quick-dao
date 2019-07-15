package io.github.yangziwen.quickdao.example.repository;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserTypedCriteriaRepositoryTest;

public class UserTypedCriteriaSpringJdbcRepositoryTest extends BaseUserTypedCriteriaRepositoryTest {

    @Override
    protected UserSpringJdbcRepository createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

    @Test
    public void testUpdateSelectiveWithCriteria() {
        Long id = 1L;
        String username = "user1_modified";
        User user = User.builder()
                .username(username)
                .build();
        TypedCriteria<User> criteria = new TypedCriteria<>(User.class)
                .and(User::getId).in(Arrays.asList(id));
        UserSpringJdbcRepository repository = createRepository();
        repository.updateSelective(user, criteria);
        Assert.assertEquals(username, repository.getById(id).getUsername());
    }

}
