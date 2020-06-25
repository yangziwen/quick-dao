package io.github.yangziwen.quickdao.example.repository.base;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;

public abstract class BaseUserSoftDeletedRepositoryTest extends BaseRepositoryTest {

    @Before
    public void before() throws Exception {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        truncateTable("user", connection);
        loadData("user_soft_deleted", connection);
    }

    @Test
    public void testGetById() {

        User user1 = createSoftDeletedRepository().getById(1L);
        Assert.assertNull(user1);

        User user2 = createSoftDeletedRepository().getById(2L);
        Assert.assertNotNull(user2);
        Assert.assertSame(2L, user2.getId());

    }

    @Test
    public void testFirst() {

        User user = createSoftDeletedRepository().firstCriteria(criteria -> criteria
                .and(User::getUsername).startWith("user"));
        Assert.assertNotNull(user);
        Assert.assertSame(2L, user.getId());

    }

    @Test
    public void testList() {
        List<User> userList = createSoftDeletedRepository().list();
        Assert.assertSame(2, userList.size());
    }

    @Test
    public void testListByIds() {
        List<User> userList = createSoftDeletedRepository().listByIds(Arrays.asList(1L, 2L));
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void count() {
        int count = createSoftDeletedRepository().count();
        Assert.assertEquals(2, count);
    }

    @Test
    public void testUpdateSelective() {

        User toUpdate = User.builder()
                .username("test")
                .build();

        createSoftDeletedRepository().updateSelective(toUpdate, criteria -> criteria
                .and(User::getUsername).startWith("user"));

        User user1 = createRepository().firstCriteria(criteria -> criteria.and(User::getUsername).eq("user1"));

        User user2 = createRepository().firstCriteria(criteria -> criteria.and(User::getUsername).eq("test"));

        // 正常数据被修改了
        Assert.assertNotNull(user2);
        Assert.assertSame(2L, user2.getId());

        // 逻辑删除的数据没有被修改
        Assert.assertNotNull(user1);
        Assert.assertSame(1L, user1.getId());
    }

    @Test
    public void testDeleteCriteria() {

        Date time = DateUtils.addSeconds(new Date(), -1);

        createSoftDeletedRepository().deleteCriteria(criteria -> criteria
                .and(User::getUsername).startWith("user"));

        User user1 = createRepository().getById(1L);

        Assert.assertTrue(user1.getUpdateTime().before(time));

        User user2 = createRepository().getById(2L);

        Assert.assertTrue(user2.getUpdateTime().after(time));

        Assert.assertEquals(0, createSoftDeletedRepository().count().intValue());

    }

    @Test
    public void testDeleteQuery() {

        Date time = DateUtils.addSeconds(new Date(), -1);

        createSoftDeletedRepository().deleteQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).startWith("user"))
                .limit(1));

        User user1 = createRepository().getById(1L);

        Assert.assertTrue(user1.getUpdateTime().before(time));

        User user2 = createRepository().getById(2L);

        Assert.assertTrue(user2.getUpdateTime().after(time));

        User user3 = createRepository().getById(3L);

        Assert.assertTrue(user3.getUpdateTime().before(time));

        Assert.assertEquals(1, createSoftDeletedRepository().count().intValue());

    }

    @Test
    public void testDeleteByIds() {

        Date time = DateUtils.addSeconds(new Date(), -1);

        createSoftDeletedRepository().deleteByIds(Arrays.asList(1L, 2L));

        User user1 = createRepository().getById(1L);

        Assert.assertTrue(user1.getUpdateTime().before(time));

        User user2 = createRepository().getById(2L);

        Assert.assertTrue(user2.getUpdateTime().after(time));

        User user3 = createRepository().getById(3L);

        Assert.assertTrue(user3.getUpdateTime().before(time));

        Assert.assertEquals(1, createSoftDeletedRepository().count().intValue());

    }

    @Test
    public void testDeleteById() {

        Date time = DateUtils.addSeconds(new Date(), -1);

        createSoftDeletedRepository().deleteById(1L);

        User user1 = createRepository().getById(1L);

        Assert.assertTrue(user1.getUpdateTime().before(time));

        createSoftDeletedRepository().deleteById(2L);

        User user2 = createRepository().getById(2L);

        Assert.assertTrue(user2.getUpdateTime().after(time));

        Assert.assertEquals(1, createSoftDeletedRepository().count().intValue());

    }

    protected abstract BaseRepository<User> createSoftDeletedRepository();

    protected abstract BaseRepository<User> createRepository();

}
