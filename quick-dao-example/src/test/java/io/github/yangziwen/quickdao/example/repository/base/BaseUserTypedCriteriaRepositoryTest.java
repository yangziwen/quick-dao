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
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Page;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.User;

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
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getCreateTime).lt(new Date());
        Query query = new Query().where(criteria);
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithQueryFromQueryMap() {
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getCreateTime).lt(new Date());
        criteria = TypedCriteria.fromParamMap(User.class, criteria.toParamMap());
        List<User> userList = createRepository().list(criteria);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithOrQuery() {
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getCreateTime).lt(new Date())
                .or()
                    .and(User::getId).eq(1L)
                    .and(User::getUsername).endWith("1")
                .end();
        Query query = new Query().where(criteria).orderBy("id", Direction.DESC).limit(2);
        List<User> userList = createRepository().list(query);
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
        criteria = TypedCriteria.fromParamMap(User.class, criteria.toParamMap());
        Query query = new Query().where(criteria).orderBy("id", Direction.DESC).limit(2);
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListContain() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Query query = new Query().where(new TypedCriteria<>(User.class)
                .and(User::getEmail).contain("@"));
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListNotContain() {
        Query query = new Query().where(new TypedCriteria<>(User.class)
                .and(User::getEmail).notContain("@"));
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNull() {
        Query query = new Query().where(new TypedCriteria<>(User.class)
                .and(User::getUsername).isNull());
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNotNull() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Query query = new Query().where(new TypedCriteria<>(User.class)
                .and(User::getUsername).isNotNull());
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListWithGroup() throws Exception {
        TypedCriteria<User> criteria = new TypedCriteria<User>(User.class)
                .and(User::getId).in(Arrays.asList(2L, 3L))
                .and(User::getUsername).endWith("2")
                .and(User::getEmail).isNotNull()
                .and(User::getCreateTime).lt(new Date())
                .or()
                    .and(User::getId).eq(1L)
                    .and(User::getUsername).endWith("1")
                .end();
        Criteria havingCriteria = new TypedCriteria<User>(User.class)
                .and(User::getCreateTime).lt(new Date());
        Query query = new Query()
                .select("create_time as createTime")
                .where(criteria)
                .groupBy("createTime")
                .having(havingCriteria)
                .limit(2);
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithLimit() {
        Query query = new Query().where(new TypedCriteria<>(User.class)
                .and(User::getId).ge(1))
                .offset(1).limit(100);
        List<User> userList = createRepository().list(query);
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
    public void testDelete() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        String username = "user1";
        TypedCriteria<User> criteria = new TypedCriteria<>(User.class)
                .and(User::getUsername).eq(username);
        BaseRepository<User> repository = createRepository();
        repository.delete(criteria);
        List<User> userList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, userList.size());
        Assert.assertNotEquals(username, userList.get(0).getUsername());
    }

    protected abstract BaseRepository<User> createRepository();

}
