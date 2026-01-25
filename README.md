
<div align="center">
    <img src="https://github.com/user-attachments/assets/d1e6907e-9bb2-4289-b9e1-ec10f362dd64" alt="描述" width="250" style="margin: 0 auto">
</div>

# QuickDAO

[![Java CI with Maven](https://github.com/yangziwen/quick-dao/actions/workflows/maven.yml/badge.svg)](https://github.com/yangziwen/quick-dao/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/yangziwen/quick-dao/badge.svg?branch=master)](https://coveralls.io/github/yangziwen/quick-dao?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/83ba3cc79e6046f69c06dbc42db00b7f)](https://www.codacy.com/gh/yangziwen/quick-dao/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yangziwen/quick-dao&amp;utm_campaign=Badge_Grade)

### 项目介绍
QuickDAO通过对Spring JDBC, MyBatis, sql2o等orm框架进行简单封装，实现增删改查方法的抽象和复用，有效消除DAO层的样板代码。

### 项目文档
[https://yangziwen.github.io/quick-dao](https://yangziwen.github.io/quick-dao/)

### 快速开始
* 引入项目依赖

```xml
<!-- 使用Spring JDBC的情形(推荐) -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-spring-jdbc</artifactId>
    <version>0.0.21</version>
</dependency>

<!-- 使用MyBatis的情形(需注意SqlSession的线程安全) -->
<!-- 建议配合使用org.mybatis.spring.SqlSessionTemplate -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-mybatis</artifactId>
    <version>0.0.21</version>
</dependency>

<!-- 使用sql2o的情形(适合快速开发demo) -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-sql2o</artifactId>
    <version>0.0.21</version>
</dependency>
```

* 定义数据的实体类<br/>
  数据库的表名、字段名默认由实体类的类名、字段名按驼峰法转下划线法获得，也可基于JPA注解显式指定

```java
@Data
@Table(name = "user")
public class User {

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Integer age;

}
```

* 数据访问类继承BaseRepository接口的实现类(以Spring JDBC为例)

```java
public class UserRepository extends BaseSpringJdbcRepository<User> {

    public UserRepository(DataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

    // UserRepository已继承了针对User对象的增删改查(包括分页)方法

}

```

* 在数据访问类中实现个性化的查询方法

```java
public List<User> listByAgeRange(int minAge, int maxAge) {
    Criteria criteria = new Criteria()
            .and("age").ge(minAge)
            .and("age").le(maxAge);
    return list(criteria);
}
```

* 使用类型安全的方式构造查询条件

```java
public List<User> listByAgeRange(int minAge, int maxAge) {
    Criteria criteria = new TypedCriteria<>(User.class)
            .and(User::getAge).ge(minAge)
            .and(User::getAge).le(maxAge);
    return list(criteria);
}
```

* 更多例子，可参考[使用手册](https://yangziwen.github.io/quick-dao/manual/)以及 [quick-dao-example](https://github.com/yangziwen/quick-dao/tree/master/quick-dao-example) 中的[单元测试](https://github.com/yangziwen/quick-dao/tree/master/quick-dao-example/src/test/java/io/github/yangziwen/quickdao/example/repository/base)
