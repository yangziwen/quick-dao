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
import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.Role;

public abstract class BaseRoleTypedCriteriaRepositoryTest extends BaseRepositoryTest {

    protected static final String tableName = "user_role";

    @Before
    public void before() throws Exception {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        truncateTable(tableName, connection);
        loadData("user_role", connection);
    }

    @Test
    public void testListWithQuery() {
        List<Role> roleList = createRepository().listQuery(query -> query
                .where(criteria -> criteria
                        .and(Role::getId).in(Arrays.asList(2L, 3L))
                        .and(Role::getUsername).endWith("2")
                        .and(Role::getRoleName).contain("visitor")
                        .and(Role::getCreateTime).lt(new Date())));
        Assert.assertEquals(1, roleList.size());
        Assert.assertEquals("user2", roleList.get(0).getUsername());
    }

    @Test
    public void testListWithQueryFromQueryMap() {
        TypedCriteria<Role> criteria = new TypedCriteria<Role>(Role.class)
                .and(Role::getId).in(new Long[] {2L, 3L})
                .and(Role::getUsername).endWith("2")
                .and(Role::getRoleName).contain("visitor")
                .and(Role::getCreateTime).lt(new Date());
        criteria = TypedCriteria.fromParamMap(Role.class, criteria.toParamMap());
        List<Role> roleList = createRepository().list(criteria);
        Assert.assertEquals(1, roleList.size());
    }

    @Test
    public void testInsert() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Role role = Role.builder()
                .id(4L)
                .username("user4")
                .roleName("visitor")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        BaseRepository<Role> repository = createRepository();
        int rows = repository.insert(role);
        Assert.assertEquals(table.getRowCount() + 1, repository.count().intValue());
        Assert.assertEquals(4L, repository.getById(4L).getId().longValue());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testBatchInsert() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Role role3 = Role.builder()
                .id(3L)
                .username("user3")
                .roleName("visitor")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        Role role4 = Role.builder()
                .id(4L)
                .username("user4")
                .roleName("visitor")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        Role role5 = Role.builder()
                .id(5L)
                .username("user5")
                .roleName("visitor")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        List<Role> roleList = Arrays.asList(role3, role4, role5);
        BaseRepository<Role> repository = createRepository();
        repository.batchInsert(roleList, 2);
        Assert.assertEquals(table.getRowCount() + roleList.size(), repository.count().intValue());
    }

    @Test
    public void testUpdate() {
        Long id = 1L;
        String username = "user1_modified";
        String roleName = "manager";
        Role role = Role.builder()
                .id(id)
                .username(username)
                .roleName(roleName)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        BaseRepository<Role> repository = createRepository();
        int rows = repository.update(role);
        Assert.assertEquals(username, repository.getById(id).getUsername());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testUpdateSelective() {
        Long id = 1L;
        String username = "user1_modified";
        Role role = Role.builder()
                .id(id)
                .username(username)
                .build();
        BaseRepository<Role> repository = createRepository();
        int rows = repository.updateSelective(role);
        Assert.assertEquals(username, repository.getById(id).getUsername());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testDeleteById() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        Long id = 1L;
        BaseRepository<Role> repository = createRepository();
        int rows = repository.deleteById(id);
        List<Role> roleList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, roleList.size());
        Assert.assertNotEquals(id, roleList.get(0).getId());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testDelete() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        String username = "user1";
        TypedCriteria<Role> criteria = new TypedCriteria<>(Role.class)
                .and(Role::getUsername).eq(username);
        BaseRepository<Role> repository = createRepository();
        int rows = repository.delete(criteria);
        List<Role> roleList = repository.list();
        Assert.assertEquals(table.getRowCount() - 1, roleList.size());
        Assert.assertNotEquals(username, roleList.get(0).getUsername());
        Assert.assertEquals(1, rows);
    }

    @Test
    public void testDeleteByIds() throws Exception {
        ITable table = loadTable(tableName, DataSourceUtils.getConnection(dataSource));
        BaseRepository<Role> repository = createRepository();
        int rows = repository.deleteByIds(Arrays.asList(1L, 2L));
        Assert.assertEquals(table.getRowCount() - 2, repository.list().size());
        Assert.assertEquals(2, rows);
    }

    protected abstract BaseRepository<Role> createRepository();

}
