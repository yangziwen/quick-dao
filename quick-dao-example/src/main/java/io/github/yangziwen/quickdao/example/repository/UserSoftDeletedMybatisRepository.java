package io.github.yangziwen.quickdao.example.repository;

import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.mybatis.BaseSoftDeletedMybatisRepository;

public class UserSoftDeletedMybatisRepository extends BaseSoftDeletedMybatisRepository<User> {

    public UserSoftDeletedMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
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
