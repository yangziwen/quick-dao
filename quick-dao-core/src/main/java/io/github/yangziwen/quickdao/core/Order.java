package io.github.yangziwen.quickdao.core;

import lombok.Getter;

public class Order {

    @Getter
    private String name;

    @Getter
    private Direction direction = Direction.ASC;

    public Order(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public enum Direction {

        ASC, DESC;

    }

}
