package io.github.yangziwen.quickdao.example.repository;

import org.junit.Assert;
import org.junit.Test;

import io.github.yangziwen.quickdao.example.entity.Role;
import io.github.yangziwen.quickdao.example.repository.base.BaseRoleTypedCriteriaRepositoryTest;

public class RoleTypedCriteriaSpringJdbcRepositoryTest extends BaseRoleTypedCriteriaRepositoryTest {

    private static final Long SECOND_ROLE_ID = 2L;

    @Override
    protected RoleSpringJdbcRepository createRepository() {
        return new RoleSpringJdbcRepository(dataSource);
    }

    /**
     * 测试按用户名查询角色
     */
    @Test
    public void testGetRoleByUsername() {
        Role role = createRepository().getRoleByUsername("user2");
        Assert.assertEquals(SECOND_ROLE_ID, role.getId());
    }

}
