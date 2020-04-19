package io.github.yangziwen.quickdao.core;

import java.util.function.Function;

import io.github.yangziwen.quickdao.core.util.VarArgsSQLFunction;
import lombok.Getter;

public class SqlFunctionExpression<E> {

    private static final VarArgsSQLFunction COUNT_FUNC = new VarArgsSQLFunction("COUNT(", ",", ")");

    private static final VarArgsSQLFunction MAX_FUNC = new VarArgsSQLFunction("MAX(", ",", ")");

    private static final VarArgsSQLFunction MIN_FUNC = new VarArgsSQLFunction("MIN(", ",", ")");

    private static final VarArgsSQLFunction AVG_FUNC = new VarArgsSQLFunction("AVG(", ",", ")");

    @Getter
    private final Class<E> classType;

    @Getter
    private VarArgsSQLFunction func;

    @Getter
    private Object[] args;

    SqlFunctionExpression(Class<E> classType) {
        this.classType = classType;
    }

    public void count() {
        this.func = COUNT_FUNC;
        this.args = new String[] { "*" };
    }

    public void count(String field) {
        this.func = COUNT_FUNC;
        this.args = new String[] { field };
    }

    public void count(Function<E, ?> getter) {
        this.func = COUNT_FUNC;
        this.args = new Function[] { getter };
    }

    public void max(String field) {
        this.func = MAX_FUNC;
        this.args = new String[] { field };
    }

    public void max(Function<E, ?> getter) {
        this.func = MAX_FUNC;
        this.args = new Function[] { getter };
    }

    public void min(String field) {
        this.func = MIN_FUNC;
        this.args = new String[] { field };
    }

    public void min(Function<E, ?> getter) {
        this.func = MIN_FUNC;
        this.args = new Function[] { getter };
    }

    public void avg(String field) {
        this.func = AVG_FUNC;
        this.args = new String[] { field };
    }

    public void avg(Function<E, ?> getter) {
        this.func = AVG_FUNC;
        this.args = new Function[] { getter };
    }

}
