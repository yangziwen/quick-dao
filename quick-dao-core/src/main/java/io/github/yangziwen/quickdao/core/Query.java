package io.github.yangziwen.quickdao.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import io.github.yangziwen.quickdao.core.Order.Direction;
import lombok.Getter;

public class Query extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    @Getter
    private List<String> selectStmtList = new ArrayList<>();

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
        selectStmtList.addAll(Arrays.asList(fields));
        return this;
    }

    public Query where(Criteria criteria) {
        this.criteria  = criteria;
        return this;
    }

    public Query groupBy(String stmt) {
        groupByList.add(stmt);
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
        criteria.fillParamMap(this);
        if (CollectionUtils.isNotEmpty(groupByList)) {
            havingCriteria.fillParamMap(this);
        }
        if (offset != 0 || limit != Integer.MAX_VALUE) {
            put(RepoKeys.OFFSET, offset);
            put(RepoKeys.LIMIT, limit);
        }
        return this;
    }

}
