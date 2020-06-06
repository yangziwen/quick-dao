package io.github.yangziwen.quickdao.core.util;

public class SqlFunctions {

    public static final VarArgsSQLFunction DISTINCT_FUNC = new VarArgsSQLFunction("distinctFunc", "DISTINCT ", ", ", "");

    public static final VarArgsSQLFunction COUNT_FUNC = new VarArgsSQLFunction("countFunc", "COUNT(", ", ", ")");

    public static final VarArgsSQLFunction COUNT_DISTINCT_FUNC = new VarArgsSQLFunction("countDistinctFunc", "COUNT(DISTINCT ", ", ", ")");

    public static final VarArgsSQLFunction MYSQL_CONCAT_FUNC = new VarArgsSQLFunction("concatFunc", "CONCAT(", ", ", ")");

    public static final VarArgsSQLFunction SQLITE_CONCAT_FUNC = new VarArgsSQLFunction("concatFunc", "", "||", "");

    public static final VarArgsSQLFunction MAX_FUNC = new VarArgsSQLFunction("maxFunc", "MAX(", ", ", ")");

    public static final VarArgsSQLFunction MIN_FUNC = new VarArgsSQLFunction("minFunc", "MIN(", ", ", ")");

    public static final VarArgsSQLFunction AVG_FUNC = new VarArgsSQLFunction("avgFunc", "AVG(", ", ", ")");

    public static final VarArgsSQLFunction SUM_FUNC = new VarArgsSQLFunction("sumFunc", "SUM(", ", ", ")");

    private SqlFunctions() {}

}
