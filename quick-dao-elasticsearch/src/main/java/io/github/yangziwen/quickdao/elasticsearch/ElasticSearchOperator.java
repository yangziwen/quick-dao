package io.github.yangziwen.quickdao.elasticsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import io.github.yangziwen.quickdao.core.Criterion;
import io.github.yangziwen.quickdao.core.Operator;

public enum ElasticSearchOperator {

    eq {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.termQuery(criterion.getQueryName(), criterion.getValue());
        }
    },

    ne {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(eq.generateQueryBuilder(criterion));
        }
    },

    gt {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.rangeQuery(criterion.getQueryName()).gt(criterion.getValue());
        }
    },

    ge {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.rangeQuery(criterion.getQueryName()).gte(criterion.getValue());
        }
    },

    lt {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.rangeQuery(criterion.getQueryName()).lt(criterion.getValue());
        }
    },

    le {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.rangeQuery(criterion.getQueryName()).lte(criterion.getValue());
        }
    },

    contain {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            String suffix = String.valueOf(criterion.getValue()).replaceAll(REGEX_ESCAPE_PATTERN, "\\\\$1");
            return QueryBuilders.regexpQuery(criterion.getQueryName(), ".*" + suffix + ".*");
        }
    },

    not_contain {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(contain.generateQueryBuilder(criterion));
        }
    },

    start_with {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.prefixQuery(criterion.getQueryName(), String.valueOf(criterion.getValue()));
        }
    },

    not_start_with {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(start_with.generateQueryBuilder(criterion));
        }
    },

    end_with {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            String suffix = String.valueOf(criterion.getValue()).replaceAll(REGEX_ESCAPE_PATTERN, "\\\\$1");
            return QueryBuilders.regexpQuery(criterion.getQueryName(), ".*" + suffix);
        }
    },

    not_end_with {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(ElasticSearchOperator.end_with.generateQueryBuilder(criterion));
        }
    },

    in {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            Collection<?> values = Collections.singletonList(criterion);
            if (criterion.getValue() instanceof Collection) {
                values = Collection.class.cast(criterion.getValue());
            }
            return QueryBuilders.termsQuery(criterion.getQueryName(), values);
        }
    },

    not_in {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(in.generateQueryBuilder(criterion));
        }
    },

    is_null {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.boolQuery().mustNot(is_not_null.generateQueryBuilder(criterion));
        }
    },

    is_not_null {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return QueryBuilders.existsQuery(criterion.getQueryName());
        }
    },

    impossible {
        @Override
        public QueryBuilder generateQueryBuilder(Criterion<?> criterion) {
            return null;
        }
    };

    private static final String REGEX_ESCAPE_PATTERN = "(\\*|\\.|\\?|\\+|\\$|\\^|\\[|\\]|\\(|\\)|\\{|\\}|\\||\\/|\\\\)";

    private static final ConcurrentMap<Operator, ElasticSearchOperator> OPERATOR_MAPPING = new ConcurrentHashMap<Operator, ElasticSearchOperator>() {
        private static final long serialVersionUID = 1L;
        {
            for (Operator operator : Operator.values()) {
                for (ElasticSearchOperator searchOperator : ElasticSearchOperator.values()) {
                    if (StringUtils.equals(operator.name(),searchOperator.name())) {
                        put(operator, searchOperator);
                    }
                }
            }
        }
    };

    public static ElasticSearchOperator from(Operator operator) {
        return OPERATOR_MAPPING.get(operator);
    }

    public abstract QueryBuilder generateQueryBuilder(Criterion<?> criterion);

}
