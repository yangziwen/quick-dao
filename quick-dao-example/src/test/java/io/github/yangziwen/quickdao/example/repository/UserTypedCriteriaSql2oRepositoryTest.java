package io.github.yangziwen.quickdao.example.repository;

import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserTypedCriteriaRepositoryTest;

public class UserTypedCriteriaSql2oRepositoryTest extends BaseUserTypedCriteriaRepositoryTest {

    @Override
    protected BaseRepository<User> createRepository() {
        Sql2o sql2o = new Sql2o(dataSource);
        return new UserSql2oRepository(sql2o);
    }

}
