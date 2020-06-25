package io.github.yangziwen.quickdao.example.repository;

import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserSoftDeletedRepositoryTest;

public class UserSoftDeletedSql2oRepositoryTest extends BaseUserSoftDeletedRepositoryTest {

    @Override
    protected BaseRepository<User> createSoftDeletedRepository() {
        return new UserSoftDeletedSql2oRepository(dataSource);
    }

    @Override
    protected BaseRepository<User> createRepository() {
        return new UserSql2oRepository(new Sql2o(dataSource));
    }

}
