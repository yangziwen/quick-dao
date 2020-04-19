package io.github.yangziwen.quickdao.core.util;

public class SqlFunctions {

    public static final VarArgsSQLFunction DISTINCT_FUNC = new VarArgsSQLFunction("DISTINCT ", ", ", "");

    public static final VarArgsSQLFunction COUNT_FUNC = new VarArgsSQLFunction("COUNT(", ", ", ")");

    public static final VarArgsSQLFunction COUNT_DISTINCT_FUNC = new VarArgsSQLFunction("COUNT(DISTINCT ", ", ", ")");

    public static final VarArgsSQLFunction MYSQL_CONCAT_FUNC = new VarArgsSQLFunction("CONCAT(", ", ", ")");

    public static final VarArgsSQLFunction SQLITE_CONCAT_FUNC = new VarArgsSQLFunction("", "||", "");

    public static final VarArgsSQLFunction MAX_FUNC = new VarArgsSQLFunction("MAX(", ", ", ")");

    public static final VarArgsSQLFunction MIN_FUNC = new VarArgsSQLFunction("MIN(", ", ", ")");

    public static final VarArgsSQLFunction AVG_FUNC = new VarArgsSQLFunction("AVG(", ", ", ")");

    public static final VarArgsSQLFunction SUM_FUNC = new VarArgsSQLFunction("SUM(", ", ", ")");

    private SqlFunctions() {}

}
