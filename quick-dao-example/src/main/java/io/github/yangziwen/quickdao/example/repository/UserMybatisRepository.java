package io.github.yangziwen.quickdao.example.repository;

import org.apache.ibatis.session.SqlSession;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.mybatis.BaseMybatisRepository;

public class UserMybatisRepository extends BaseMybatisRepository<User> {

    protected UserMybatisRepository(SqlSession sqlSession) {
        super(sqlSession);
    }

    public User getUserByEmail(String email) {
        Criteria criteria = new Criteria().and("email").eq(email);
        return first(new Query().where(criteria));
    }

}
