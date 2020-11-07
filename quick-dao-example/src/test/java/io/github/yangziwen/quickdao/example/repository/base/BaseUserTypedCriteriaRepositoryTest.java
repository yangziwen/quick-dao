package io.github.yangziwen.quickdao.example.repository.base;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Page;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;

public abstract class BaseUserTypedCriteriaRepositoryTest extends BaseRepositoryTest {

    private static final String tableName = "user";

    @Before
    public void before() throws Exception {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        truncateTable(tableName, connection);
        loadData(tableName, connection);
    }

    @Test
    public void testListWithQuery() {
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getId).in(Arrays.asList(2L, 3L))
                        .and(User::getUsername).endWith("2")
                        .and(User::getEmail).isNotNull()
                        .and(User::getGender).eq(Gender.FEMALE)
                        .and(User::getCreateTime).lt(new Date())));
        Assert.assertEquals(1, userList.size());
        Assert.assertEquals("user2", userList.get(0).getUsername());
    }

    @Test
    public void testListWithQueryFromQueryMap() {
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getGender).eq(Gender.FEMALE)
                .and(User::getCreateTime).lt(new Date());
        criteria = TypedCriteria.fromParamMap(User.class, criteria.toParamMap());
        List<User> userList = createRepository().list(criteria);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithOrQuery() {
        List<User> userList = createRepository().listQuery(query -> query
                .select(User::getId)
                .select(User::getUsername)
                .select("create_time").as(User::getCreateTime)
                .where(criteria -> criteria
                        .and(User::getId).in(Arrays.asList(2L, 3L))
                        .and(User::getUsername).endWith("2")
                        .and(User::getEmail).isNotNull()
                        .and(User::getCreateTime).lt(new Date())
                        .or()
                            .and(User::getId).eq(1L)
                            .and(User::getUsername).endWith("1")
                        .end())
                .orderBy(User::getId, Direction.DESC)
                .limit(2));
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListWithOrQueryFromQueryMap() {
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getCreateTime).lt(new Date())
                .or()
                    .and(User::getId).eq(1L)
                    .and(User::getUsername).endWith("1")
                .end();
        List<User> userList = createRepository().listQuery(query -> query
                .where(TypedCriteria.fromParamMap(User.class, criteria.toParamMap()))
                .orderBy(User::getId, Direction.DESC)
                .limit(2));
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListContain() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria.and(User::getEmail).contain("@")));
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListNotContain() {
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria.and(User::getEmail).notContain("@")));
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNull() {
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria.and(User::getUsername).isNull()));
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNotNull() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria.and(User::getUsername).isNotNull()));
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListWithGroup() throws Exception {
        List<User> userList = createRepository().listQuery(query -> query
                .select(User::getCreateTime)
                .where(criteria -> criteria
                        .and(User::getId).in(Arrays.asList(2L, 3L))
                        .and(User::getUsername).endWith("2")
                        .and(User::getEmail).isNotNull()
                        .and(User::getCreateTime).lt(new Date())
                        .or()
                            .and(User::getId).eq(1L)
                            .and(User::getUsername).endWith("1")
                        .end())
                .groupBy(User::getCreateTime)
                .having(criteria -> criteria
                        .and(User::getCreateTime).lt(new Date()))
                .limit(2));
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithLimit() {
        List<User> userList = createRepository().listQuery(query -> query
                .where(criteria -> criteria.and(User::getId).ge(1L))
                .offset(1).limit(100));
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testCount() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Integer count = createRepository().count();
        Assert.assertEquals(table.getRowCount(), count.intValue());
    }

    @Test
    public void testPaginate() {
        Page<User> userPage = createRepository().paginate(new Query(), 2, 1);
        Assert.assertEquals(1, userPage.getList().size());
        Assert.assertEquals(2, userPage.getTotalCount().intValue());
    }

    @Test
    public void testUpdateSelectiveWithCriteria() {
        Long id = 1L;
        String username = "user1_modified";
        User user = User.builder()
                .username(username)
                .build();
        BaseRepository<User> repository = createRepository();
        int rows = createRepository().updateSelective(user, criteria -> criteria.and(User::getId).in(Arrays.asList(id)));
        Assert.assertEquals(username, repository.getById(id).getUsername());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testDelete() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        String username = "user1";
        BaseRepository<User> repository = createRepository();
        int rows = repository.deleteCriteria(criteria -> criteria.and(User::getUsername).eq(username));
        List<User> userList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, userList.size());
        Assert.assertNotEquals(username, userList.get(0).getUsername());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testSelectOfExpression() {
        User user = createRepository().firstQuery(query -> query
                .selectExpr(expr -> expr.max(User::getId)).as(User::getId)
                .selectExpr(expr -> expr.min("update_time")).as(User::getUpdateTime)
                .select("max(create_time)").as("createTime"));
        Assert.assertEquals(2L, user.getId().longValue());
    }

    protected abstract BaseRepository<User> createRepository();

}
