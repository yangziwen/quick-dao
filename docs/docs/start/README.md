# 快速开始

## 简介
QuickDAO通过对Spring JDBC、MyBatis、sql2o等ORM框架进行简单封装，实现增删改查方法的抽象和复用，有效消除DAO层的样板代码。

## 引入依赖
基于Spring JDBC、MyBatis、sql2o的三种依赖引入其一即可。
<br/>
推荐使用基于Spring JDBC的封装，在快速实现轻量级原型时也可考虑使用基于sql2o的封装。
```xml
<!-- 使用Spring JDBC的情形(推荐) -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-spring-jdbc</artifactId>
    <version>0.0.19</version>
</dependency>
```
```xml
<!-- 使用sql2o的情形(适合快速开发轻量级的原型) -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-sql2o</artifactId>
    <version>0.0.19</version>
</dependency>
```
```xml
<!-- 使用MyBatis的情形(需注意SqlSession的线程安全) -->
<!-- 建议配合org.mybatis.spring.SqlSessionTemplate使用 -->
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-mybatis</artifactId>
    <version>0.0.19</version>
</dependency>
```

## 定义数据实体类
数据库的表名、字段名默认由实体类的类名、字段名按驼峰法转下划线法获得，也可以在`javax.persistence.Table`和`javax.persistence.Column`注解中显式指定。
```java
@Data
@Table(name = "user")
public class User {

    @Id
    @Column
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Gender gender;

    @Column
    private Integer age;

}
```

## 实现数据访问类
声明一个UserRepository数据访问类，并继承[BaseRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseRepository.java)接口或[BaseReadOnlyRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseReadOnlyRepository.java)接口的某个实现类(以Spring JDBC为例)。
```java
public class UserRepository extends BaseSpringJdbcRepository<User> {

    public UserRepository(DataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

}
```

其中BaseReadOnlyRepository接口的实现类中包含以下查询方法以及各种变体
* getById
* first
* list
* listByIds
* count
* paginate

而BaseRepository接口的实现类中不仅包含上述的查询方法，还包含以下的增删改操作
* insert
* batchInsert
* update
* updateSelective
* delete
* deleteById
* deleteByIds

## 使用Java DSL构造SQL
在上面定义的UserRepository数据访问类中，可以进一步实现个性化的增删改查方法。其中的查询SQL，可以通过Java DSL声明。
例如按年龄范围查找用户信息的方法，可按如下方式实现。
```java
public List<User> listByAgeRange(int minAge, int maxAge) {
    Criteria criteria = new Criteria()
            .and("age").ge(minAge)
            .and("age").le(maxAge);
    return list(criteria);
}
```
以上代码通过[Criteria](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/Criteria.java)对象声明查询条件，会构造如下的SQL。
```sql
SELECT
  `id` AS 'id',
  `username` AS 'username',
  `email` AS 'email',
  `gender` AS 'gender',
  `age` AS 'age'
FROM `user`
WHERE `age` >= ${minAge}
  AND `age` <= ${maxAge}
```
然而上述实现通过字符串对查询条件中的字段进行声明，并不能保证字段的正确性，对后续可能发生的字段名称的修改也不友好。

因此，还可以通过getter方法的lambda表达式来声明查询条件中的字段，从而保证DSL的正确性和类型安全。
```java
public List<User> listByAgeRange(int minAge, int maxAge) {
    Criteria criteria = new TypedCriteria<>(User.class)
            .and(User::getAge).ge(minAge)
            .and(User::getAge).le(maxAge);
    return list(criteria);
}
```

此外，如果SQL中涉及到`group by`，`having`、`order by`、`limit`等操作，则可通过[Query](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/Query.java)对象以更完整的方式构造SQL。
```java
public List<String> listUsernameOfEldestMaleUser(int limit) {
    Query query = newTypedQuery()
            .select(User::getUsername)
            .where(criteria -> criteria
                    .and(User::getGender).eq(Gender.MALE))
            .orderBy(User::getAge, Direction.DESC)
            .limit(limit);
    return list(query).stream()
            .map(User::getUsername)
            .collect(Collectors.toList());
}
```
注意，如果实体中定义了枚举类型的字段，且数据库中对应的字段类型为int时，枚举类需要实现[IEnum](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/IEnum.java)接口的getValue()方法，数据访问操作才能将代码中的枚举对象正确的转换成int数值。

另外，`Criteria`和`Query`均有基于lambda表达式的类型安全的版本，分别是[TypedCriteria](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/TypedQuery.java)和[TypedQuery](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/TypedQuery.java)。

更多`Query`对象和`Criteria`对象的使用方法，可以参考[quick-dao-example](https://github.com/yangziwen/quick-dao/tree/master/quick-dao-example)中的[单元测试](https://github.com/yangziwen/quick-dao/tree/master/quick-dao-example/src/test/java/io/github/yangziwen/quickdao/example/repository/base)。