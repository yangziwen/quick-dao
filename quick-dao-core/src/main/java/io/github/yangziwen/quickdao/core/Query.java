package io.github.yangziwen.quickdao.core;

import java.util.List;

import io.github.yangziwen.quickdao.core.Order.Direction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

public class Query {

    @Getter
    private List<String> fieldList = new ArrayList<>();

    @Getter
    private Criteria criteria;

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

    public Query(Criteria criteria) {
        this.criteria = criteria;
    }

    public Query select(String...fields) {
        fieldList.addAll(Arrays.asList(fields));
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
