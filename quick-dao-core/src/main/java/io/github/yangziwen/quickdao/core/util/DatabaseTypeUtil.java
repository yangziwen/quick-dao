package io.github.yangziwen.quickdao.core.util;

import org.apache.commons.lang3.StringUtils;

public class DatabaseTypeUtil {

    private static final String DATABASE_TYPE_KEY = "QUICK_DAO_DATABASE_TYPE";

    private static final DatabaseType DATABASE_TYPE = detectDatabaseType();

    private DatabaseTypeUtil() {}

    private static DatabaseType detectDatabaseType() {

        String type = "";

        if (StringUtils.isBlank(type)) {
            type = System.getProperty(DATABASE_TYPE_KEY);
        }

        if (StringUtils.isBlank(type)) {
            type = System.getProperty(DATABASE_TYPE_KEY.toLowerCase());
        }

        if (StringUtils.isBlank(type)) {
            type = System.getenv(DATABASE_TYPE_KEY);
        }

        if (StringUtils.isBlank(type)) {
            type = System.getenv(DATABASE_TYPE_KEY.toLowerCase());
        }

        DatabaseType dbType = DatabaseType.of(type);

        if (dbType != DatabaseType.UNKNOWN) {
            return dbType;
        }

        if (doesClassExist("com.mysql.jdbc.jdbc2.optional.MysqlDataSource")) {
            return DatabaseType.MYSQL;
        }

        if (doesClassExist("com.mysql.cj.jdbc.MysqlDataSource")) {
            return DatabaseType.MYSQL;
        }

        if (doesClassExist("org.sqlite.SQLiteDataSource")) {
            return DatabaseType.SQLITE;
        }

        return DatabaseType.UNKNOWN;
    }

    private static boolean doesClassExist(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static DatabaseType getDatabaseType() {
        return DATABASE_TYPE;
    }

    public static enum DatabaseType {

        UNKNOWN,

        MYSQL,

        SQLITE;

        static DatabaseType of(String value) {
            if (StringUtils.isBlank(value)) {
                return UNKNOWN;
            }
            for (DatabaseType type : values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return UNKNOWN;
        }

    }

}
