package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import io.github.yangziwen.quickdao.example.repository.base.BaseRoleTypedCriteriaRepositoryTest;
import io.github.yangziwen.quickdao.mybatis.CustomEnumTypeHandler;

public class RoleTypedCriteriaMybatisRepositoryTest extends BaseRoleTypedCriteriaRepositoryTest {

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
    protected RoleMybatisRepository createRepository() {
        return new RoleMybatisRepository(sqlSessionFactory.openSession());
    }

}
