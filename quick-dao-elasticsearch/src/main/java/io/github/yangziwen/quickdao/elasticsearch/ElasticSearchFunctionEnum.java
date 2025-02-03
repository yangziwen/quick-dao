package io.github.yangziwen.quickdao.elasticsearch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import io.github.yangziwen.quickdao.core.FunctionStmt;
import io.github.yangziwen.quickdao.core.util.SqlFunctions;
import io.github.yangziwen.quickdao.core.util.VarArgsSQLFunction;

public enum ElasticSearchFunctionEnum {

    DISTINCT_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.cardinality(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    COUNT_DISTINCT_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.cardinality(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    COUNT_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.count(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    MAX_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.max(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    MIN_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.min(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    AVG_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.avg(stmt.getStmtAlias()).field(getField(stmt));
        }
    },

    SUM_FUNC {
        @Override
        public AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt) {
            return AggregationBuilders.sum(stmt.getStmtAlias()).field(getField(stmt));
        }
    };

    private static final ConcurrentMap<VarArgsSQLFunction, ElasticSearchFunctionEnum> FUNC_MAPPING = new ConcurrentHashMap<VarArgsSQLFunction, ElasticSearchFunctionEnum>() {
        private static final long serialVersionUID = 1L;
        {
            put(SqlFunctions.DISTINCT_FUNC, DISTINCT_FUNC);
            put(SqlFunctions.COUNT_DISTINCT_FUNC, COUNT_DISTINCT_FUNC);
            put(SqlFunctions.COUNT_FUNC, COUNT_FUNC);
            put(SqlFunctions.MAX_FUNC, MAX_FUNC);
            put(SqlFunctions.MIN_FUNC, MIN_FUNC);
            put(SqlFunctions.AVG_FUNC, AVG_FUNC);
            put(SqlFunctions.SUM_FUNC, SUM_FUNC);
        }
    };

    protected abstract AggregationBuilder doGenerateAggsBuilder(FunctionStmt<?> stmt);

    public static AggregationBuilder generateAggsBuilder(FunctionStmt<?> stmt) {
        ElasticSearchFunctionEnum func = FUNC_MAPPING.get(stmt.getExpression().getFunc());
        if (func == null) {
            return null;
        }
        return func.doGenerateAggsBuilder(stmt);
    }

    @SuppressWarnings("unchecked")
    private static <T> String getField(FunctionStmt<T> stmt) {
        if (stmt.getField() != null) {
            return stmt.getField();
        }
        Function<T, ?> getter = ((Function<T, ?>[]) stmt.getExpression().getArgs())[0];
        return stmt.getExtractor().extractFieldNameFromGetter(getter);
    }

}
