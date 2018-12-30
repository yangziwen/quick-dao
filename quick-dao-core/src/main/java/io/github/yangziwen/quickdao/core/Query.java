package io.github.yangziwen.quickdao.core;

import java.util.List;

import io.github.yangziwen.quickdao.core.Order.Direction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

public class Query {

    @Getter
    private List<String> selectStmtList = new ArrayList<>();

    @Getter
    private Criteria criteria = new Criteria();;

    @Getter
    private List<String> groupByList = new ArrayList<>();

    @Getter
    private Criteria havingCriteria = new Criteria();

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

}
