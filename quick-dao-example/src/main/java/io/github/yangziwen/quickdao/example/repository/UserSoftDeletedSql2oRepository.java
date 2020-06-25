package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.sql2o.BaseSoftDeletedSql2oRepository;

public class UserSoftDeletedSql2oRepository extends BaseSoftDeletedSql2oRepository<User> {

    public UserSoftDeletedSql2oRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getDeletedFlagColumn() {
        return "valid";
    }

    @Override
    public Object getDeletedFlagValue() {
        return false;
    }

    @Override
    public Object getNotDeletedFlagValue() {
        return true;
    }

    @Override
    public String getUpdateTimeColumn() {
        return "update_time";
    }

    @Override
    public Object getUpdateTimeValue() {
        return "now()";
    }

}
