package io.github.yangziwen.quickdao.springjdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import io.github.yangziwen.quickdao.core.IEnum;

/**
 * 通过 JDK 动态代理伪造 ResultSet / ResultSetMetaData，
 * 不依赖 mockito 即可测试 mapRow 的完整映射逻辑（含 IEnum 列转换）。
 */
public class BeanPropertyRowMapperTest {

    enum Color implements IEnum<Color, Integer> {
        RED(1), GREEN(2);
        private Integer value;
        Color(Integer value) { this.value = value; }
        @Override
        public Integer getValue() { return value; }
    }

    public static class TestUser {
        private Long id;
        private String userName;
        private Color color;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
    }

    @Test
    public void testMapRowWithAllColumns() throws Exception {
        ResultSet rs = newResultSet(
                new String[] {"id", "user_name", "color"},
                new Object[] {1L, "user1", 1});

        TestUser user = BeanPropertyRowMapper.newInstance(TestUser.class).mapRow(rs, 0);

        Assert.assertEquals(Long.valueOf(1L), user.getId());
        Assert.assertEquals("user1", user.getUserName());
        Assert.assertEquals(Color.RED, user.getColor());
    }

    @Test
    public void testMapRowWithUnknownColumnIgnored() throws Exception {
        ResultSet rs = newResultSet(
                new String[] {"id", "not_exist_column"},
                new Object[] {2L, "x"});

        TestUser user = new BeanPropertyRowMapper<>(TestUser.class).mapRow(rs, 0);

        Assert.assertEquals(Long.valueOf(2L), user.getId());
        Assert.assertNull(user.getUserName());
    }

    @Test
    public void testMapRowWithUnknownEnumValueMapsToNull() throws Exception {
        ResultSet rs = newResultSet(
                new String[] {"id", "color"},
                new Object[] {3L, 99});

        TestUser user = new BeanPropertyRowMapper<>(TestUser.class).mapRow(rs, 0);

        Assert.assertEquals(Long.valueOf(3L), user.getId());
        Assert.assertNull(user.getColor());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testMapRowWithCheckFullyPopulatedAndMissingColumn() throws Exception {
        ResultSet rs = newResultSet(
                new String[] {"id"},
                new Object[] {4L});

        new BeanPropertyRowMapper<>(TestUser.class, true).mapRow(rs, 0);
    }

    @Test
    public void testGetterSetters() {
        BeanPropertyRowMapper<TestUser> mapper = new BeanPropertyRowMapper<>();
        mapper.setMappedClass(TestUser.class);
        Assert.assertEquals(TestUser.class, mapper.getMappedClass());
        Assert.assertFalse(mapper.isCheckFullyPopulated());
        Assert.assertFalse(mapper.isPrimitivesDefaultedForNullValue());
        mapper.setCheckFullyPopulated(true);
        Assert.assertTrue(mapper.isCheckFullyPopulated());
        mapper.setPrimitivesDefaultedForNullValue(true);
        Assert.assertTrue(mapper.isPrimitivesDefaultedForNullValue());
        Assert.assertNotNull(mapper.getConversionService());
    }

    private static ResultSet newResultSet(String[] columns, Object[] row) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "getMetaData":
                    return Proxy.newProxyInstance(
                            ResultSetMetaData.class.getClassLoader(),
                            new Class<?>[] {ResultSetMetaData.class},
                            (p, m, a) -> handleMetaData(columns, m.getName(), a));
                case "getInt":
                    return ((Number) row[index(args)]).intValue();
                case "getLong":
                    return ((Number) row[index(args)]).longValue();
                case "getString":
                    return (String) row[index(args)];
                case "getObject":
                    return row[index(args)];
                case "wasNull":
                    return false;
                default:
                    throw new UnsupportedOperationException(name);
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(), new Class<?>[] {ResultSet.class}, handler);
    }

    private static Object handleMetaData(String[] columns, String methodName, Object[] args) {
        switch (methodName) {
            case "getColumnCount":
                return columns.length;
            case "getColumnLabel":
            case "getColumnName":
                return columns[index(args)];
            default:
                throw new UnsupportedOperationException(methodName);
        }
    }

    private static int index(Object[] args) {
        return ((Integer) args[0]) - 1;
    }

}
