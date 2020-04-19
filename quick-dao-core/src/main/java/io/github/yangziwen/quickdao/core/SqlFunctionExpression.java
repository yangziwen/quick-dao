package io.github.yangziwen.quickdao.core;

import java.util.function.Function;

import io.github.yangziwen.quickdao.core.util.DatabaseTypeUtil;
import io.github.yangziwen.quickdao.core.util.DatabaseTypeUtil.DatabaseType;
import io.github.yangziwen.quickdao.core.util.SqlFunctions;
import io.github.yangziwen.quickdao.core.util.VarArgsSQLFunction;
import lombok.Getter;

public class SqlFunctionExpression<E> {

    @Getter
    private final Class<E> classType;

    @Getter
    private VarArgsSQLFunction func;

    @Getter
    private Object[] args;

    SqlFunctionExpression(Class<E> classType) {
        this.classType = classType;
    }

    public void distinct(String arg) {
        this.func = SqlFunctions.DISTINCT_FUNC;
        this.args = new String[] { arg };
    }

    public void distinct(Function<E, ?> getter) {
        this.func = SqlFunctions.DISTINCT_FUNC;
        this.args = new Function[] { getter };
    }

    public void countDistinct(String arg) {
        this.func = SqlFunctions.COUNT_DISTINCT_FUNC;
        this.args = new String[] { arg };
    }

    public void countDistinct(Function<E, ?> getter) {
        this.func = SqlFunctions.COUNT_DISTINCT_FUNC;
        this.args = new Function[] { getter };
    }

    public void concat(String...args) {
        DatabaseType type = DatabaseTypeUtil.getDatabaseType();
        if (type == DatabaseType.SQLITE) {
            func = SqlFunctions.SQLITE_CONCAT_FUNC;
        } else {
            func = SqlFunctions.MYSQL_CONCAT_FUNC;
        }
        this.args = args;
    }

    @SuppressWarnings("unchecked")
    public void concat(Function<E, ?>...getters) {
        DatabaseType type = DatabaseTypeUtil.getDatabaseType();
        if (type == DatabaseType.SQLITE) {
            func = SqlFunctions.SQLITE_CONCAT_FUNC;
        } else {
            func = SqlFunctions.MYSQL_CONCAT_FUNC;
        }
        this.args = getters;
    }

    public void count() {
        this.func = SqlFunctions.COUNT_FUNC;
        this.args = new String[] { "*" };
    }

    public void count(String arg) {
        this.func = SqlFunctions.COUNT_FUNC;
        this.args = new String[] { arg };
    }

    public void count(Function<E, ?> getter) {
        this.func = SqlFunctions.COUNT_FUNC;
        this.args = new Function[] { getter };
    }

    public void max(String arg) {
        this.func = SqlFunctions.MAX_FUNC;
        this.args = new String[] { arg };
    }

    public void max(Function<E, ?> getter) {
        this.func = SqlFunctions.MAX_FUNC;
        this.args = new Function[] { getter };
    }

    public void min(String arg) {
        this.func = SqlFunctions.MIN_FUNC;
        this.args = new String[] { arg };
    }

    public void min(Function<E, ?> getter) {
        this.func = SqlFunctions.MIN_FUNC;
        this.args = new Function[] { getter };
    }

    public void avg(String arg) {
        this.func = SqlFunctions.AVG_FUNC;
        this.args = new String[] { arg };
    }

    public void avg(Function<E, ?> getter) {
        this.func = SqlFunctions.AVG_FUNC;
        this.args = new Function[] { getter };
    }

    public void sum(String arg) {
        this.func = SqlFunctions.SUM_FUNC;
        this.args = new String[] { arg };
    }

    public void sum(Function<E, ?> getter) {
        this.func = SqlFunctions.SUM_FUNC;
        this.args = new Function[] { getter };
    }

}
