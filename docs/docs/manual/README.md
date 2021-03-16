# 使用手册

## 构建数据访问类
基于实体类的泛型声明，QuickDAO的数据访问类([BaseRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-spring-jdbc/src/main/java/io/github/yangziwen/quickdao/springjdbc/BaseSpringJdbcRepository.java))为用户提供了基本的增删改查功能。在使用时，用户可以根据自身的需要，选用基于不同ORM框架的封装实现。

下面以基于Spring JDBC封装的[BaseSpringJdbcRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-spring-jdbc/src/main/java/io/github/yangziwen/quickdao/springjdbc/BaseSpringJdbcRepository.java)为例，说明如何构建指定实体类的数据访问类。

### 引入依赖
```xml
<dependency>
    <groupId>io.github.yangziwen</groupId>
    <artifactId>quick-dao-spring-jdbc</artifactId>
    <version>0.0.20</version>
</dependency>
```

### 声明实体类
注意，使用`@Table`修饰的实体和使用`@Column`修饰的字段，才会被QuickDAO使用。<br/>
数据库的表名、字段名默认由实体类的类名、字段名按驼峰法转下划线法获得，也可以在`@Table`和`@Column`注解中显式指定。<br/>
可通过添加`@GeneratedValue`注解实现基于数据库主键自增的id，否则需要在插入数据前手动填充id（例如使用snowflake生成全局唯一id的场景）。
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
    private Gender gender;

    @Column
    private Integer age;

}
```

### 实现数据访问类
```java
public class UserRepository extends BaseSpringJdbcRepository<User> {
    
    public UserRepository(DataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

}
```
`UserRepository`类即可针对`User`实体类为用户提供基本的增删改查和分页查询的功能。<br/>

其中[BaseSpringJdbcReadOnlyRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-spring-jdbc/src/main/java/io/github/yangziwen/quickdao/springjdbc/BaseSpringJdbcReadOnlyRepository.java)接口的实现类中包含以下查询方法以及各种变体
* getById
* first
* list
* listByIds
* count
* paginate

而[BaseSpringJdbcRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-spring-jdbc/src/main/java/io/github/yangziwen/quickdao/springjdbc/BaseSpringJdbcRepository.java)接口的实现类中不仅包含上述的查询方法，还包含以下的增删改操作
* insert
* batchInsert
* update
* updateSelective
* delete
* deleteById
* deleteByIds

在`UserRepository`类上添加`@Repository`注解，同时在构造方法上添加`@Autowired`注解，即可将数据访问类的实例托管给Spring容器维护。

### 添加个性化方法
建议将查询条件的构造过程实现在数据访问类中，对外仅暴露语义明确的方法，尽量不要将构造查询条件的过程泄漏到数据访问层以外。
```java
public class UserRepository extends BaseSpringJdbcRepository<User> {

    public UserRepository(DataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

    public List<User> listByUsernameStartWith(String usernamePrefix) {
        return listCriteria(criteria -> criteria
                .and(User:getUsername).startWith(usernamePrefix));
    }

}
```

### 配置字段包装符号
当数据表中存在一些与数据库关键字同名的字段时，则需要使用字段包装符号，才能正常操作这些字段。例如
```sql
SELECT `id`, `order`, `group` FROM `data` WHERE `order` = 1;
```

对于不同的数据库，字段包装符号会有所不同。

例如MySQL数据库的字段包装符号如下
| 字段类型 | 包装符号 | 示例 |
| :-: | :-: | :-: |
| 表名 | `` | ``` SELECT * FROM `user` ``` |
| 字段名 | `` | ``` SELECT `username` FROM `user` ``` |
| 别名 | '' | ``` SELECT `username` AS 'name' FROM `user` ```|

对于不同ORM框架，变量占位符的包装符号也会有所不同。

例如Spring JDBC的变量占位符是`:username`的形式，而Mybatis的变量占位符是`#{username}`的形式。

这就要求我们在声明数据访问类时，能够定制化的配置这些字段包装符号。

仍以MySQL和Spring JDBC的组合为例，可按如下方式声明字段的包装符号。
```java
public class UserRepository extends BaseSpringJdbcRepository<User> {

    public UserRepository(DataSource dataSource) {
        super(new JdbcTemplate(dataSource), new SqlGenerator(
            new StringWrapper("`", "`"),
            new StringWrapper("`", "`"),
            new StringWrapper("'", "'"),
            new StringWrapper(":", "")));
    }

}
```
包装符号的配置，是通过显式的调用[SqlGenerator](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/SqlGenerator.java)的以下构造方法实现的。
```java
public SqlGenerator(
        StringWrapper tableWrapper,
        StringWrapper columnWrapper,
        StringWrapper aliasWrapper,
        StringWrapper placeholderWrapper) {
    this.tableWrapper = tableWrapper;
    this.columnWrapper = columnWrapper;
    this.aliasWrapper = aliasWrapper;
    this.placeholderWrapper = placeholderWrapper;
}
```

## 修改操作
[BaseRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseRepository.java)接口的实现类中提供了以下修改数据的方法

### 插入数据
```java
// 插入一条记录，并回填id
void insert(E entity);

// 插入多条记录（方法内部会执行集合的判空操作）
void batchInsert(List<E> entities);

// 插入多条记录，并按指定数值分批次插入
void batchInsert(List<E> entities, int batchSize);
```
参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| E | entity | 实体对象 |
| List&#60;E&#62; | entities | 实体对象集合 |
| int | batchSize | 插入批次数量 |

### 更新数据
```java
// 按entity中的主键id更新一条记录，主键字段由@Id注解指定
void update(E entity);

// 按entity中的主键id更新一条记录，仅会更新entity中的非空字段
void updateSelective(E entity);

// 按过滤条件更新多条记录，且仅会更新entity中的非空字段
void updateSelective(E entity, Criteria criteria);

// 按过滤条件更新多条记录，且仅会更新entity中的非空字段
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造过滤条件
void updateSelective(E entity, Consumer<TypedCriteria<E>> consumer);
```
参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| E | entity | 实体对象 |
| Criteria | criteria | 过滤条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造过滤条件的函数式接口 |

### 删除数据
```java
// 按主键id删除一条记录
void deleteById(Object id);

// 按主键id集合删除多条数据
void deleteByIds(Collection<?> ids);

// 按过滤条件删除数据
void delete(Criteria criteria);

// 按过滤条件删除数据
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造过滤条件
void deleteCriteria(Consumer<TypedCriteria<E>> consumer);

// 按过滤条件删除数据，用于支持需要设定limit的情形
void delete(Query query);

// 按过滤条件删除数据，用于支持需要设定limit的情形
// 方法内部会向consumer入参注入一个TypedQuery对象，用于构造过滤条件
void deleteQuery(Consumer<TypedQuery<E>> consumer);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| Object | id | 主键id |
| Criteria | criteria | 过滤条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造过滤条件的函数式接口 |
| Query | query | 过滤条件，支持limit参数 |
| Consumer&#60;TypedQuery&#60;E&#62;&#62; | consumer | 用于构造过滤条件的函数式接口 |

## 查询操作
[BaseRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseRepository.java)接口和[BaseReadOnlyRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseReadOnlyRepository.java)接口的实现类中提供了以下查询数据的方法


### 按id查询结果
```java
// 按主键id查询一条记录
E getById(Object id);

// 按主键id集合查询多条数据
List<E> listByIds(Collection<?> ids);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| Object | id | 主键id |
| Collection<?> | ids | 主键id集合 |

### 查询单个结果
```java
// 按条件查询单条数据
E first(Criteria criteria);

// 按条件查询单条数据
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造查询条件
E firstCriteria(Consumer<TypedCriteria<E>> consumer);

// 按条件查询单条数据，并支持聚合、排序等前提要求
E first(Query query);

// 按条件查询单条数据，并支持聚合、排序等前提要求
// 方法内部会向consumer入参注入一个TypedQuery对象，用于构造查询条件
E firstQuery(Consumer<TypedQuery<E>> consumer);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| Criteria | criteria | 查询条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |
| Query | query | 查询条件，支持`group by`和`having`操作 |
| Consumer&#60;TypedQuery&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |

### 查询列表结果
```java
// 按条件查询多条数据
List<E> list(Criteria criteria);

// 按条件查询多条数据
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造查询条件
List<E> listCriteria(Consumer<TypedCriteria<E>> consumer);

// 按条件查询单条数据，并支持聚合、排序等前提要求
List<E> list(Query query);

// 按条件查询单条数据，并支持聚合、排序等前提要求
// 方法内部会向consumer入参注入一个TypedQuery对象，用于构造查询条件
List<E> listQuery(Consumer<TypedQuery<E>> consumer);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| Criteria | criteria | 查询条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |
| Query | query | 查询条件，支持`group by`和`having`操作 |
| Consumer&#60;TypedQuery&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |

### 查询数量结果
```java
// 按条件查询数量
Integer count(Criteria criteria);

// 按条件查询数量
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造查询条件
Integer countCriteria(Consumer<TypedCriteria<E>> consumer);

// 按条件查询数量，并支持聚合等前提要求
Integer count(Query query);

// 按条件查询数量，并支持聚合等前提要求
// 方法内部会向consumer入参注入一个TypedQuery对象，用于构造查询条件
Integer countQuery(Consumer<TypedQuery<E>> consumer);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| Criteria | criteria | 查询条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |
| Query | query | 查询条件，支持`group by`和`having`操作 |
| Consumer&#60;TypedQuery&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |

### 查询分页结果
```java
// 按条件查询分页结果
Page<E> paginate(Criteria criteria, int pageNo, int pageSize);

// 按条件查询分页结果
// 方法内部会向consumer入参注入一个TypedCriteria对象，用于构造查询条件
Page<E> paginateCriteria(Consumer<TypedCriteria<E>> consumer, int pageNo, int pageSize);

// 按条件查询分页结果，并支持聚合等要求
Page<E> paginate(Query query, int pageNo, int pageSize);

// 按条件查询分页结果，并支持聚合等前提要求
// 方法内部会向consumer入参注入一个TypedQuery对象，用于构造查询条件
Page<E> paginateQuery(Consumer<TypedQuery<E>> consumer, int pageNo, int pageSize);
```

参数说明
| 类型 | 参数名 | 描述 |
| :-: | :-: | :-: |
| int | pageNo | 当前页号，从1开始计数 |
| int | pageSize | 每页数据条数 |
| Criteria | criteria | 查询条件 |
| Consumer&#60;TypedCriteria&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |
| Query | query | 查询条件，支持`group by`和`having`操作 |
| Consumer&#60;TypedQuery&#60;E&#62;&#62; | consumer | 用于构造查询条件的函数式接口 |

## 构造查询条件
QuickDAO基于Java DSL构造查询语句，其中`Criteria`对象（包括`TypedCriteria`）代表`WHERE`和`HAVING`后的一系列过滤条件，而`Query`对象（包括`TypedQuery`）代表整个SQL语句，可指定`SELECT`的字段、`GROUP BY`的字段、`ORDER BY`的方式、`LIMIT`的限制，以及为`WHERE`和`HAVING`设置相应的`Criteria`对象。
<br/>
<br/>
在编写Java DSL的过程中，用户可以选择使用字符串来声明字段，也可以基于getter方法的lambda表达是来声明字段。<br/>
* 使用字符串声明字段时，既可以使用数据库表中的原始字段名，也可以使用Java实体类中的字段名，同时还可以在`Query`对象的`select`方法中使用各种数据库函数。当使用Java实体类中的字段名编写DSL时，QuickDAO会自动完成向数据库表中原始字段名的转换。<br/>
* 使用基于getter方法的lambda表达式声明字段时，QuickDAO会根据`TypedCriteria`或者`TypedQuery`对象声明的泛型，对lambda表达式进行编译期检查，可有效避免DSL中的字段声明错误。`TypedQuery`对象暴露了`selectExpr`方法，也可以支持少数常用数据库函数结合这种lambda表达式的形式进行调用。

### 简单查询条件
当SQL中仅需要指定`WHERE`后的查询条件时，可直接使用`Criteria`对象编写DSL

使用字符串指定字段的方式
```java
new Criteria()
    .and("email").endWith("@qq.com")
    .and("age").ge(20)
    .and("age").le(30);
```
或使用lambda表达式指定字段的方式
```java
new TypedCriteria<>(User.class)
    .and(User::getEmail).endWith("@qq.com")
    .and(User::getAge).ge(20)
    .and(User::getAge).le(30);
```
以上DSL会生成如下SQL
```sql
SELECT
  `id` AS 'id',
  `username` AS 'username',
  `email` AS 'email',
  `gender` AS 'gender',
  `age` AS 'age'
FROM `user`
WHERE `email` LIKE '%@qq.com'
  AND `age` >= 20
  AND `age` <= 30
```

### 复杂查询条件
当SQL中需要指定查询字段、聚合方式、排序方式等条件时，需要使用`Query`对象编写DSL

使用字符串指定字段的方式
```java
new Query()
    .select("username")
    .where(new Criteria()
        .and("gender").eq(Gender.MALE))
    .orderBy("age", Direction.DESC)
    .limit(10);
```

使用lambda表达式指定字段的方式
```java
new TypedQuery<>(User.class)
    .select(User::getUsername)
    .where(criteria -> criteria
        .and(User::getGender).eq(Gender.MALE))
    .orderBy(User::getAge, Direction.DESC)
    .limit(10);
```

以上DSL会生成如下SQL
```sql
SELECT
  `username` AS 'username'
FROM `user`
WHERE `gender` = 1
ORDER BY `age` DESC
LIMIT 10
```
枚举类Gender在声明时实现了[IEnum](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/IEnum.java)接口，可以参见[Gender](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-example/src/main/java/io/github/yangziwen/quickdao/example/enums/Gender.java)。

### 聚合查询条件
`Query`对象对聚合查询也提供了支持

使用字符串指定字段的方式
```java
new Query()
    .select("gender")
    .select("avg(`age`) AS 'age'")
    .groupBy("gender")
    .having(new Criteria().and("min(`age`)").ge(20))
    .orderBy("gender");
```

使用lambda表达式指定字段的方式
```java
new TypedQuery<>(User.class)
    .select(User::getGender)
    .selectExpr(expr -> expr.avg(User::getAge)).as(User::getAge)
    .groupBy(User::getGender)
    .having(criteria -> criteria.andExpr(expr -> expr.min(User::getAge)).ge(20))
    .orderBy(User::getGender);
```

以上DSL会生成如下SQL
```sql
SELECT
  `gender` AS 'gender',
  AVG(`age`) AS 'age'
FROM `user`
GROUP BY `gender`
HAVING MIN(`avg`) >= 20
ORDER BY `GENDER`
```

### 嵌套查询条件
有些情况下，`WHERE`中的各种`AND`和`OR`的条件可能会涉及嵌套，可按如下方式编写DSL。

使用字符串指定字段的方式
```java
new Criteria()
    .or()
        .and("username").startWith("张")
        .and("gender").eq(Gender.MALE)
    .end()
    .or()
        .and("username").startWith("李")
        .and("gender").eq(Gender.FEMALE)
    .end();
```

使用lambda表达式指定字段的方式
```java
new TypedCriteria<>(User.class)
    .or()
        .and(User::getUsername).startWith("张")
        .and(User::getGender).eq(Gender.MALE)
    .end()
    .or()
        .and(User::getUsername).startWith("李")
        .and(User::getGender).eq(Gender.FEMALE)
    .end();
```

以上DSL会生成如下查询条件
```sql
WHERE (`username` LIKE '张%' AND gender = 1) OR (`username` LIKE '李%' AND gender = 2)
```

同样的，也可以在`AND`中嵌套`OR`条件，例如
```java
new Criteria()
    .and()
        .or("username").startWith("张")
        .or("username").startWith("李")
    .end()
    .and()
        .or("age").le(20)
        .or("age").ge(30)
    .end();
```
上述DSL会生成如下查询条件
```sql
WHERE (`username` LIKE '张%' OR `username` LIKE '李%') AND (`age` <= 20 OR `age` >= 30)
```

### 基于断言的查询条件
QuickDAO还提供了基于断言的查询条件构造方式，在保持链式的DSL的前提下，提供了动态控制查询条件拼装的能力。

使用字符串指定字段的方式
```java
public Criteria toCriteria(String username, Integer minAge, Integer maxAge) {
    new Criteria()
        .ifValid(StringUtils.isNotEmpty(username))
        .then("username").contain(StringUtils.trim(username))

        .ifValid(minAge != null)
        .then("age").ge(minAge)

        .ifValid(maxAge != null)
        .then("age").le(maxAge);
}
```

使用lambda表达式指定字段的方式
```java
public TypedCriteria<User> toCriteria(String username, Integer minAge, Integer maxAge) {
    return new TypedCriteria<>(User.class)
        .ifValid(() -> StringUtils.isNotBlank(username))
        .then(User::getUsername).contain(StringUtils.trim(username))

        .ifValid(() -> minAge != null)
        .then(User::getAge).ge(minAge)

        .ifValid(() -> maxAge != null)
        .then(User::getAge).le(maxAge);
}
```

### 条件比较运算符
上述构造`Criteria`条件的过程中，使用了各种比较运算符。QuickDAO目前支持的[比较运算符](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/Operator.java)如下表所示。
| 方法名 | <div style="width: 100px">描述</div> | <div style="width: 440px">示例</div> |
| :-: | :-: | :-: |
| eq | 判断相等 | ```criteria.and("username").eq("zhangsan")``` |
| ne | 判断不等 | ```criteria.and("username").ne("zhangsan")```  |
| gt | 判断大于 | ```criteria.and("age").gt(20)```  |
| ge | 判断大于等于 | ```criteria.and("age").ge(21)``` |
| lt | 判断小于 | ```criteria.and("age").lt(30)``` |
| le | 判断小于等于 | ```criteria.and("age").le(29)``` |
| contain | 判断包含指定字符串 | ```criteria.and("email").contain("@qq")``` |
| notContain | 判断不包含指定字符串 | ```criteria.and("email").notContain("@qq")``` |
| startWith | 判断以指定字符串开始 | ```criteria.and("email").startWith("zhangsan")``` |
| notStartWith | 判断不以指定字符串开始 | ```criteria.and("email").notStartWith("zhangsan")``` |
| endWith | 判断以指定字符串结束 | ```criteria.and("email").endWith("@qq.com")``` |
| notEndWith | 判断不以指定字符串结束 | ```criteria.and("email").notEndWith("@qq.com")``` |
| in | 判断包含于集合或数组中 | ```criteria.and("id").in(Arrays.asList(1L, 2L))``` |
| notIn | 判断不包含于集合或数组中 | ```criteria.and("id").notIn(Arrays.asList(1L, 2L))``` |
| isNull | 判断为空 | ```criteria.and("username").isNull()``` |
| isNotNull | 判断不为空 | ```criteria.and("username").isNotNull()``` |

## 使用数据库函数
当使用字符串指定字段时，可以在构造`SELECT`、`WHERE`、`HAVING`的过程中使用任意的数据库函数。

在使用lambda表达式指定字段的方式时，QuickDAO提供了`selectExpr`、`andExpr`、`orExpr`等方法，用来支持类型安全的数据库函数构造。

但是目前的API仅能支持一些常用的函数，如果需要使用API支持范围以外的数据库函数，则只能使用字符串的表达方式。

另外，虽然`DISTINCT`是SQL的关键字，但在API中也被实现成了一种函数。

API支持的函数列表如下所示，详情请见[SqlFunctionExpression](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/SqlFunctionExpression.java)
| 方法声明 | 入参 | 功能描述 |
| :-: | :-: | :-: |
| distinct | String 或 Function | 计算去重后的内容 |
| countDistinct | String 或 Function | 计算去重后的数量 |
| concat | String... 或 Function... | 连接字符串 |
| count | String 或 Function | 计算数量 |
| max | String 或 Function | 计算最大值 |
| min | String 或 Function | 计算最小值 |
| avg | String 或 Function | 计算平均值 |
| sum | String 或 Function | 计算总和 |

使用字符串指定字段的方式
```java
new Query()
    .select("gender")
    .select("avg(`age`) AS 'age'")
    .groupBy("gender")
    .having(new Criteria().and("min(`age`)").ge(20))
    .orderBy("gender");
```

使用lambda表达式指定字段的方式
```java
new TypedQuery<>(User.class)
    .select(User::getGender)
    .selectExpr(expr -> expr.avg(User::getAge)).as(User::getAge)
    .groupBy(User::getGender)
    .having(criteria -> criteria.andExpr(expr -> expr.min(User::getAge)).ge(20))
    .orderBy(User::getGender);
```

也可以混合使用字符串和lambda表达式
```java
new TypedQuery<>(User.class)
    .select(User::getGender)
    .selectExpr(expr -> expr.avg("age")).as("age")
    .groupBy("gender")
    .having(criteria -> criteria.andExpr(expr -> expr.min("age")).ge(20))
    .orderBy("gender");
```

## 如何处理枚举
ORM框架（如Spring JDBC、Mybatis等）在处理枚举类型的字段时，一般会将枚举的name或者ordinal值写入数据库，但是这种处理方式可能无法完全满足开发的需求。

例如用varchar类型保存枚举字段可能在存储效率上不够理想，而存储ordinal的值，则会导致枚举的值与他们在枚举类中声明的顺序强绑定。

因此，一些团队会为每个枚举类显式的声明一个value字段，然后在用于数据持久化的实体类中，使用Integer类型的字段替换枚举字段。这种方式能够避免上述提到的问题，但是在编写代码时（例如赋值或者比较的逻辑），会涉及各种枚举字段与整数的转换，不仅丧失了使用枚举的可读性和安全性，同时增加了代码的繁琐性，以及引入了转换错误的潜在风险。

所以有没有既能在实体类中直接声明和使用枚举类型的字段，又能使用枚举中显式声明的value值来进行数据存储的方式呢？<br/>

为此，QuickDAO中提供了[IEnum](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/IEnum.java)接口。当一个枚举类实现了`IEnum`接口中的`getValue`方法后，数据持久化和查询的过程中，QuickDAO就会自动完成实体中该枚举类的字段与value值之间的转换。这里`IEnum`的`getValue`方法返回的不一定需要是整数，也可以是字符串或者其他类型，返回类型由实现`IEnum`接口时声明的泛型决定。

以`User`实体类中的`Gender`枚举为例，给出如下的枚举实现。
```java
public enum Gender implements IEnum<Gender, Integer> {

    MALE(1),

    FEMALE(2);

    private Integer value;

    Gender(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
```

## 逻辑删除的实现
一些团队在开发项目过程中习惯使用逻辑删除（Soft Delete）来替代物理删除（Hard Delete）操作，即使用类似`is_deleted`的字段来标记数据是否被删除。

这种做法可以带来一些好处，比如在发生误删除时可以比较容易的找回数据，同时已删除的数据在商业分析和审计等层面可能也存在一定价值。

但是，逻辑删除的缺点也很明显，例如所有的查询操作都要携带`is_deleted = false`的条件，同时所有的删除操作都要按修改操作来实现。如果这些针对逻辑删除的细节都在应用程序中实现，则会给代码的编写和维护带来额外的复杂度，同时一旦开发人员在某处遗漏了对逻辑删除标识的判断，就会引发bug。

从本质上讲，应用程序的业务逻辑不应感知数据的持久化操作到底是物理删除还是逻辑删除，这就要求数据持久化框架能够统一的抽象并支持切换物理删除和逻辑删除的相关操作。

QuickDAO从这一观点出发，扩展出了[BaseSoftDeletedRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/BaseSoftDeletedRepository.java)的各种实现，对外暴露了统一的增删改查API，很好的屏蔽了逻辑删除的实现细节。以下将以[BaseSoftDeletedSpringJdbcRepository](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-spring-jdbc/src/main/java/io/github/yangziwen/quickdao/springjdbc/BaseSoftDeletedSpringJdbcRepository.java)为例，来说明逻辑删除的实现方法。

首先是表结构的声明，需要声明逻辑删除标识字段`is_deleted`及其**默认值**。
```sql
CREATE TABLE `user` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(128) NOT NULL,
    `email` VARCHAR(128) NOT NULL,
    `gender` INT(11),
    `age` INT(11),
    `update_time` DATETIME,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0'
);
```

之后是声明数据实体类。需要注意的是，业务代码并不关心删除操作是否为逻辑删除，因此实体类中也不应声明`is_deleted`对应的成员变量，插入数据时也不会指定`is_deleted`字段的值，这也是声明表结构时一定要为`is_deleted`字段声明默认值的原因。
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
    private Gender gender;

    @Column
    private Integer age;

}
```

最后按如下方式声明数据实体类对应的数据访问类，即可实现逻辑删除功能。
```java
public class UserRepository extends BaseSoftDeletedSpringJdbcRepository<User> {

    public UserSoftDeletedSpringJdbcRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * 逻辑删除的标识字段（不需要在entity中声明）
     */
    @Override
    public String getDeletedFlagColumn() {
        return "is_deleted";
    }

    /**
     * 已删除数据的逻辑删除标识字段值
     * 对于`is_deleted`字段，true表示已删除
     */
    @Override
    public Object getDeletedFlagValue() {
        return true;
    }

    /**
     * 未删除数据的逻辑删除标识字段值
     * 对于`is_deleted`字段，false表示未删除
     */
    @Override
    public Object getNotDeletedFlagValue() {
        return false;
    }

    /**
     * 数据表中的更新时间字段，返回空则逻辑删除时忽略更新时间
     */
    @Override
    public String getUpdateTimeColumn() {
        return "update_time";
    }

    /**
     * 数据表中更新时间字段的取值，返回空则逻辑删除时忽略更新时间
     * 只能返回new Date().getTime() 或 "now()"，不能返回Date对象
     */
    @Override
    public Object getUpdateTimeValue() {
        return "now()";
    }

}
```

通常情况下，一个项目中所有数据表的逻辑删除标识字段名称以及取值应是统一的，因此在项目范围内可实现一个继承了`BaseSoftDeletedSpringJdbcRepository`的抽象类，对逻辑删除标识字段和更新时间字段作统一声明。

