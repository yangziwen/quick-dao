package io.github.yangziwen.quickdao.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import io.github.yangziwen.quickdao.core.Order.Direction;
import lombok.Getter;

public class Query extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    @Getter
    private List<Stmt> selectStmtList = new ArrayList<>();

    @Getter
    private Criteria criteria = new Criteria();

    @Getter
    private List<String> groupByList = new ArrayList<>();

    @Getter
    private Criteria havingCriteria = new Criteria(null, 0 + RepoKeys.HAVING);

    @Getter
    private int offset = 0;

    @Getter
    private int limit = Integer.MAX_VALUE;

    @Getter
    private List<Order> orderList = new ArrayList<>();

    public Query() { }

    public Query select(String...fields) {
        this.selectStmtList.addAll(Arrays
                .stream(fields)
                .map(Stmt::new)
                .collect(Collectors.toList()));
        return this;
    }

    public InnerQuery select(String field) {
        return this.new InnerQuery(new Stmt(field));
    }

    public Query select(Stmt stmt) {
        this.selectStmtList.add(stmt);
        return this;
    }

    public Query where(Criteria criteria) {
        this.criteria  = criteria;
        return this;
    }

    public Query groupBy(String stmt) {
        this.groupByList.add(stmt);
        return this;
    }

    public Query having(Criteria criteria) {
        criteria.setKey(RepoKeys.HAVING);
        this.havingCriteria = criteria;
        return this;
    }

    public Query orderBy(String name, Direction direction) {
        this.orderList.add(new Order(name, direction));
        return this;
    }

    public Query orderBy(String name) {
        return this.orderBy(name, Direction.ASC);
    }

    public Query offset(int offset) {
        this.offset = offset;
        return this;
    }

    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Map<String, Object> asMap() {
        return this;
    }

    public Map<String, Object> toParamMap() {
        clear();
        criteria.fillParamMap(this);
        if (CollectionUtils.isNotEmpty(groupByList)) {
            havingCriteria.fillParamMap(this);
        }
        if (offset != 0 || limit != Integer.MAX_VALUE) {
            if (offset != 0) {
                put(RepoKeys.OFFSET, offset);
            }
            put(RepoKeys.LIMIT, limit);
        }
        return this;
    }

    public class InnerQuery {

        Stmt stmt;

        public InnerQuery(Stmt stmt) {
            this.stmt = stmt;
        }

        public Query as(String alias) {
            this.stmt.setAlias(alias);
            return Query.this.select(this.stmt);
        }

        public Query select(String...fields) {
            return Query.this.select(this.stmt).select(fields);
        }

        public InnerQuery select(String field) {
            return Query.this.select(this.stmt).select(field);
        }

        public Query where(Criteria criteria) {
            return Query.this.select(this.stmt).where(criteria);
        }

        public Query groupBy(String stmt) {
            return Query.this.select(this.stmt).groupBy(stmt);
        }

        public Query having(Criteria criteria) {
            return Query.this.select(this.stmt).having(criteria);
        }

        public Query orderBy(String name, Direction direction) {
            return Query.this.select(this.stmt).orderBy(name, direction);
        }

        public Query orderBy(String name) {
            return Query.this.select(this.stmt).orderBy(name);
        }

        public Query offset(int offset) {
            return Query.this.select(this.stmt).offset(offset);
        }

        public Query limit(int limit) {
            return Query.this.select(this.stmt).limit(limit);
        }

        public Map<String, Object> asMap() {
            return Query.this.select(this.stmt);
        }

        public Map<String, Object> toParamMap() {
            return Query.this.select(this.stmt).toParamMap();
        }

    }

}
