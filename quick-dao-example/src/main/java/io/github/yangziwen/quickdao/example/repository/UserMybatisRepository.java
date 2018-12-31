package io.github.yangziwen.quickdao.example.repository;

import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.mybatis.BaseMybatisRepository;

public class UserMybatisRepository extends BaseMybatisRepository<User> {

    protected UserMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
    }

}
