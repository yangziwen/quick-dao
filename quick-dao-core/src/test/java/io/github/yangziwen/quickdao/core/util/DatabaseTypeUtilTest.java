package io.github.yangziwen.quickdao.core.util;

import org.junit.Assert;
import org.junit.Test;

import io.github.yangziwen.quickdao.core.util.DatabaseTypeUtil.DatabaseType;

public class DatabaseTypeUtilTest {

    @Test
    public void testDatabaseTypeOfWithBlank() {
        Assert.assertEquals(DatabaseType.UNKNOWN, DatabaseType.of(""));
        Assert.assertEquals(DatabaseType.UNKNOWN, DatabaseType.of("   "));
        Assert.assertEquals(DatabaseType.UNKNOWN, DatabaseType.of(null));
    }

    @Test
    public void testDatabaseTypeOfWithValidNames() {
        Assert.assertEquals(DatabaseType.MYSQL, DatabaseType.of("mysql"));
        Assert.assertEquals(DatabaseType.MYSQL, DatabaseType.of("MYSQL"));
        Assert.assertEquals(DatabaseType.MYSQL, DatabaseType.of("MySQL"));
        Assert.assertEquals(DatabaseType.SQLITE, DatabaseType.of("sqlite"));
        Assert.assertEquals(DatabaseType.SQLITE, DatabaseType.of("SQLITE"));
        Assert.assertEquals(DatabaseType.UNKNOWN, DatabaseType.of("unknown"));
        Assert.assertEquals(DatabaseType.UNKNOWN, DatabaseType.of("oracle"));
    }

    @Test
    public void testGetDatabaseTypeReturnsNotNull() {
        Assert.assertNotNull(DatabaseTypeUtil.getDatabaseType());
    }

}
