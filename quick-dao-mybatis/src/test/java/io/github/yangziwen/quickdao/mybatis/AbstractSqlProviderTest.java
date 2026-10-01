package io.github.yangziwen.quickdao.mybatis;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.junit.Assert;
import org.junit.Test;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;

public class AbstractSqlProviderTest {

    public static class TestUserSqlProvider extends AbstractSqlProvider<TestUser> {
    }

    private final TestUserSqlProvider provider = new TestUserSqlProvider();

    @Test
    public void testGetById() {
        String sql = provider.getById(1L);
        Assert.assertTrue(sql, sql.contains("SELECT"));
        Assert.assertTrue(sql, sql.contains("FROM t_user"));
        Assert.assertTrue(sql, sql.contains("id = #{id}"));
    }

    @Test
    public void testList() {
        Query query = new Query()
                .where(new Criteria().and("age").gt(18))
                .orderBy("age");
        String sql = provider.list(query);
        Assert.assertTrue(sql, sql.contains("SELECT"));
        Assert.assertTrue(sql, sql.contains("FROM t_user"));
        Assert.assertTrue(sql, sql.contains("age > #{"));
        Assert.assertTrue(sql, sql.contains("ORDER BY age asc"));
    }

    @Test
    public void testCount() {
        Query query = new Query().where(new Criteria().and("age").gt(18));
        String sql = provider.count(query);
        Assert.assertTrue(sql, sql.contains("SELECT COUNT(*)"));
        Assert.assertTrue(sql, sql.contains("FROM t_user"));
        Assert.assertTrue(sql, sql.contains("age > #{"));
    }

    @Test
    public void testDeleteAndDeleteById() {
        Query query = new Query().where(new Criteria().and("age").gt(100));
        String sql = provider.delete(query);
        Assert.assertTrue(sql, sql.contains("DELETE FROM t_user"));
        Assert.assertTrue(sql, sql.contains("age > #{"));

        Assert.assertTrue(provider.deleteById(1L).contains("DELETE FROM t_user"));
    }

    @Test
    public void testInsert() {
        String sql = provider.insert(new TestUser());
        Assert.assertTrue(sql, sql.contains("INSERT INTO t_user"));
        Assert.assertTrue(sql, sql.contains("#{id}"));
        Assert.assertTrue(sql, sql.contains("#{age}"));
        Assert.assertTrue(sql, sql.contains("#{username}"));
    }

    @Test
    public void testBatchInsert() {
        String sql = provider.batchInsert(Arrays.asList(new TestUser(), new TestUser()));
        Assert.assertTrue(sql, sql.contains("INSERT INTO t_user"));
    }

    @Test
    public void testUpdate() {
        String sql = provider.update(new TestUser());
        Assert.assertTrue(sql, sql.contains("UPDATE t_user"));
        Assert.assertTrue(sql, sql.contains("SET age = #{age}"));
        Assert.assertTrue(sql, sql.contains("user_name = #{username}"));
        Assert.assertTrue(sql, sql.contains("WHERE id = #{id}"));
    }

    @Test
    public void testUpdateSelective() throws Exception {
        TestUser user = new TestUser();
        Field ageField = TestUser.class.getDeclaredField("age");
        ageField.setAccessible(true);
        ageField.set(user, 20);

        String sql = provider.updateSelective(user);
        Assert.assertTrue(sql, sql.contains("UPDATE t_user"));
        Assert.assertTrue(sql, sql.contains("age = #{age}"));
        Assert.assertFalse(sql, sql.contains("username = #{username}"));
    }

    @Table(name = "t_user")
    public static class TestUser {
        @Id
        @Column(name = "id")
        private Long id;
        @Column(name = "age")
        private Integer age;
        @Column(name = "user_name")
        private String username;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

}
