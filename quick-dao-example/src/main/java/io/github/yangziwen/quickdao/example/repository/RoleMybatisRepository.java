package io.github.yangziwen.quickdao.example.repository;

import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.example.entity.Role;
import io.github.yangziwen.quickdao.mybatis.BaseMybatisRepository;

public class RoleMybatisRepository extends BaseMybatisRepository<Role> {

    protected RoleMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
    }

}
