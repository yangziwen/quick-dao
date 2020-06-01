# 使用手册

## 构建数据访问类

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
* 使用基于getter方法的lambda表达式声明字段时，QuickDAO将会根据`TypedCriteria`或者`TypedQuery`对象声明的泛型，对lambda表达式进行编译期检查，可有效避免DSL中的字段声明错误。`TypedQuery`对象暴露了`selectExpr`方法，也可以支持少数常用数据库函数结合这种lambda表达式的形式进行调用。

### 构造简单查询
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
以上DSL将会生成如下SQL
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

### 构造复杂查询
当SQL中需要指定查询字段、聚合方式、排序方式等条件时，需要使用`Criteria`对象编写DSL

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

以上DSL将会生成如下SQL
```sql
SELECT
  `username` AS 'username'
FROM `user`
WHERE `gender` = 1
ORDER BY `age` DESC
LIMIT 10
```
枚举类Gender在声明时实现了[IEnum](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-core/src/main/java/io/github/yangziwen/quickdao/core/IEnum.java)接口，可以参见[Gender](https://github.com/yangziwen/quick-dao/blob/master/quick-dao-example/src/main/java/io/github/yangziwen/quickdao/example/enums/Gender.java)。

### 构造聚合查询
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

以上DSL将会生成如下SQL
```sql
SELECT
  `gender` AS 'gender',
  AVG(`age`) AS 'age'
FROM `user`
GROUP BY `gender`
HAVING MIN(`avg`) >= 20
ORDER BY `GENDER`
```

### 构造嵌套条件查询

## 使用数据库函数

## 如何处理枚举

## 逻辑删除的实现