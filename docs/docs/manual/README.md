# 使用手册

## 构建数据访问类

## 修改操作

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

### 按id查询结果

### 查询单个结果

### 查询集合结果

### 查询数量结果

### 查询分页结果

### 构造简单查询

### 构造复杂查询

### 构造聚合查询

### 构造嵌套条件查询

### 使用常用函数

## 枚举的处理

## 逻辑删除的实现