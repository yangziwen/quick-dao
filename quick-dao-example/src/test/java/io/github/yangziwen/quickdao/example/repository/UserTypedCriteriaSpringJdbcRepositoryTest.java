package io.github.yangziwen.quickdao.example.repository;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserTypedCriteriaRepositoryTest;

public class UserTypedCriteriaSpringJdbcRepositoryTest extends BaseUserTypedCriteriaRepositoryTest {

    @Override
    protected UserSpringJdbcRepository createRepository() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return new UserSpringJdbcRepository(template);
    }

    @Test
    public void testGetUserListByComplicatedCriteria() {
        TypedCriteria<User> criteria = new TypedCriteria<>(User.class)
                .and(User::getUsername).startWith("user")
                .and()
                    .or(User::getGender).eq(Gender.MALE)
                    .or(User::getGender).eq(Gender.FEMALE)
                .end()
                .and()
                    .or(User::getCreateTime).le(new Date())
                .end()
                .or()
                    .and(User::getId).eq(1L)
                .end();
        TypedCriteria<User> newCriteria = TypedCriteria.fromParamMap(User.class, criteria.toParamMap());
        List<User> list = createRepository().list(criteria);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(criteria.toParamMap().toString(), newCriteria.toParamMap().toString());
    }

}
