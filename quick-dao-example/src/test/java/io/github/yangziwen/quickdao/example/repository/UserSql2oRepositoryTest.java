package io.github.yangziwen.quickdao.example.repository;

import org.sql2o.Sql2o;

public class UserSql2oRepositoryTest extends BaseUserRepositoryTest {

    @Override
    protected UserSql2oRepository createRepository() {
        Sql2o sql2o = new Sql2o(dataSource);
        return new UserSql2oRepository(sql2o);
    }

}
