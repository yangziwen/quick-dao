package io.github.yangziwen.quickdao.core;

import io.github.yangziwen.quickdao.core.util.VarArgsSQLFunction;

public enum Operator {

    eq {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " = " + placeholder;
        }
    },

    ne {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " != " + placeholder;
        }
    },

    gt {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " > " + placeholder;
        }
    },

    ge {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " >= " + placeholder;
        }
    },

    lt {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " < " + placeholder;
        }
    },

    le {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " <= " + placeholder;
        }
    },

    contain {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " LIKE " + chooseConcatFunc().render("'%'", placeholder, "'%'");
        }
    },

    not_contain {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " NOT LIKE " + chooseConcatFunc().render("'%'", placeholder, "'%'");
        }
    },

    start_with {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " LIKE " + chooseConcatFunc().render(placeholder, "'%'");
        }
    },

    not_start_with {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " NOT LIKE " + chooseConcatFunc().render(placeholder, "'%'");
        }
    },

    end_with {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " LIKE " + chooseConcatFunc().render("'%'", placeholder);
        }
    },

    not_end_with {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " NOT LIKE " + chooseConcatFunc().render("'%'", placeholder);
        }
    },

    in {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " IN (" + placeholder + ")" ;
        }
    },

    not_in {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " NOT IN (" + placeholder + ")";
        }
    },

    is_null {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " IS NULL ";
        }
    },

    is_not_null {
        @Override
        public String buildCondition(String columnName, String placeholder) {
            return columnName + " IS NOT NULL";
        }
    };

    private static final VarArgsSQLFunction MYSQL_CONCAT_FUNC = new VarArgsSQLFunction("CONCAT(", ", ", ")");

    private static final VarArgsSQLFunction SQLITE_CONCAT_FUNC = new VarArgsSQLFunction("", "||", "");

    public abstract String buildCondition(String columnName, String placeholder);

    private static VarArgsSQLFunction chooseConcatFunc() {
        return MYSQL_CONCAT_FUNC;
    }

}
