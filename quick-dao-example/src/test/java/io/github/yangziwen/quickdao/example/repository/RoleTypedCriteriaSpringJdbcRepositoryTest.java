package io.github.yangziwen.quickdao.example.repository;

import io.github.yangziwen.quickdao.example.repository.base.BaseRoleTypedCriteriaRepositoryTest;

public class RoleTypedCriteriaSpringJdbcRepositoryTest extends BaseRoleTypedCriteriaRepositoryTest {

    @Override
    protected RoleSpringJdbcRepository createRepository() {
        return new RoleSpringJdbcRepository(dataSource);
    }

}
