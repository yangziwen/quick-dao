package io.github.yangziwen.quickdao.example.repository;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.Role;
import io.github.yangziwen.quickdao.example.repository.base.BaseRoleTypedCriteriaRepositoryTest;

public class RoleTypedCriteriaSql2oRepositoryTest extends BaseRoleTypedCriteriaRepositoryTest {

    @Override
    protected BaseRepository<Role> createRepository() {
        return new RoleSql2oRepository(dataSource);
    }

}
