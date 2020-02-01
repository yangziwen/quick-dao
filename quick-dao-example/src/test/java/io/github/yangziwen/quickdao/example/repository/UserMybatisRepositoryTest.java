package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.junit.Assert;
import org.junit.Test;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.repository.base.BaseUserRepositoryTest;
import io.github.yangziwen.quickdao.mybatis.CustomEnumTypeHandler;

public class UserMybatisRepositoryTest extends BaseUserRepositoryTest {

    private static SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(dataSource);

    private static SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        TransactionFactory transactionFactory = new ManagedTransactionFactory();
        Environment environment = new Environment("test", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setCacheEnabled(false);
        configuration.setLazyLoadingEnabled(false);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setDefaultEnumTypeHandler(CustomEnumTypeHandler.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    protected UserMybatisRepository createRepository() {
        return new UserMybatisRepository(sqlSessionFactory.openSession());
    }

    @Test
    public void testGetUserByEmail() {
        String email = "user1@test.com";
        User user = createRepository().getUserByEmail(email);
        Assert.assertNotNull(user);
        Assert.assertEquals(email, user.getEmail());
    }

}
