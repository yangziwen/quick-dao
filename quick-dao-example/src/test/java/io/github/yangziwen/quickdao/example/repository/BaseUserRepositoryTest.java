package io.github.yangziwen.quickdao.example.repository;

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
import io.github.yangziwen.quickdao.example.entity.User;

public abstract class BaseUserRepositoryTest extends BaseRepositoryTest {

    private static final String tableName = "user";

    @Before
    public void before() throws Exception {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        truncateTable(tableName, connection);
        loadData(tableName, connection);
    }

    @Test
    public void testGetById() {
        User user = createRepository().getById(1L);
        Assert.assertNotNull(user);
        Assert.assertSame(1L, user.getId());
    }

    @Test
    public void testFirst() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Long expectedId = getLongValue(table, 1, "id");
        User user = createRepository().first(new Query().orderBy("id", Direction.DESC));
        Assert.assertEquals(expectedId, user.getId());
    }

    @Test
    public void testList() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        List<User> userList = createRepository().list();
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListWithQuery() {
        Criteria criteria = new Criteria()
                .and("id").in(Arrays.asList(2L, 3L))
                .and("username").endWith("2")
                .and("email").isNotNull()
                .and("createTime").lt(new Date());
        Query query = new Query().where(criteria);
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithOrQuery() {
        Criteria criteria = new Criteria()
                .and("id").in(Arrays.asList(2L, 3L))
                .and("username").endWith("2")
                .and("email").isNotNull()
                .and("createTime").lt(new Date())
                .or()
                    .and("id").eq(1L)
                    .and("username").endWith("1")
                .end();
        Query query = new Query().where(criteria).orderBy("id", Direction.DESC).limit(2);
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListContain() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Query query = new Query().where(new Criteria().and("email").contain("@"));
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListNotContain() {
        Query query = new Query().where(new Criteria().and("email").notContain("@"));
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNull() {
        Query query = new Query().where(new Criteria().and("username").isNull());
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListIsNotNull() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Query query = new Query().where(new Criteria().and("username").isNotNull());
        List<User> userList = createRepository().list(query);
        Assert.assertEquals(table.getRowCount(), userList.size());
    }

    @Test
    public void testListWithGroup() throws Exception {
        Criteria criteria = new Criteria()
                .and("id").in(Arrays.asList(2L, 3L))
                .and("username").endWith("2")
                .and("email").isNotNull()
                .and("createTime").lt(new Date())
                .or()
                    .and("id").eq(1L)
                    .and("username").endWith("1")
                .end();
        Criteria havingCriteria = new Criteria()
                .and("createTime").lt(new Date());
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
        Query query = new Query().where(new Criteria().and("id").ge(1)).offset(1).limit(100);
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
    public void testInsert() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        User user = User.builder()
                .username("user3")
                .email("user3@test.com")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        BaseRepository<User> repository = createRepository();
        repository.insert(user);
        Assert.assertEquals(table.getRowCount() + 1, repository.count().intValue());
        Assert.assertNotNull(user.getId());
    }

    @Test
    public void testBatchInsert() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        User user3 = User.builder()
                .username("user3")
                .email("user3@test.com")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        User user4 = User.builder()
                .username("user4")
                .email("user4@test.com")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        User user5 = User.builder()
                .username("user5")
                .email("user5@test.com")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        List<User> userList = Arrays.asList(user3, user4, user5);
        BaseRepository<User> repository = createRepository();
        repository.batchInsert(userList, 2);
        Assert.assertEquals(table.getRowCount() + userList.size(), repository.count().intValue());
    }

    @Test
    public void testUpdate() {
        Long id = 1L;
        String username = "user1_modified";
        String email = "user1_modified@test.com";
        User user = User.builder()
                .id(id)
                .username(username)
                .email(email)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        BaseRepository<User> repository = createRepository();
        repository.update(user);
        Assert.assertEquals(username, repository.getById(id).getUsername());
    }

    @Test
    public void testUpdateSelective() {
        Long id = 1L;
        String username = "user1_modified";
        User user = User.builder()
                .id(id)
                .username(username)
                .build();
        BaseRepository<User> repository = createRepository();
        repository.updateSelective(user);
        Assert.assertEquals(username, repository.getById(id).getUsername());
    }

    @Test
    public void testDeleteById() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Long id = 1L;
        BaseRepository<User> repository = createRepository();
        repository.deleteById(id);
        List<User> userList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, userList.size());
        Assert.assertNotEquals(id, userList.get(0).getId());
    }

    @Test
    public void testDelete() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        String username = "user1";
        Criteria criteria = new Criteria().and("username").eq(username);
        BaseRepository<User> repository = createRepository();
        repository.delete(criteria);
        List<User> userList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, userList.size());
        Assert.assertNotEquals(username, userList.get(0).getUsername());
    }

    protected abstract BaseRepository<User> createRepository();

}
