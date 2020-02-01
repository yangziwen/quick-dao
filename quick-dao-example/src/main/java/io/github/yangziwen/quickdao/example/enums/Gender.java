package io.github.yangziwen.quickdao.example.enums;

import io.github.yangziwen.quickdao.core.IEnum;

public enum Gender implements IEnum<Gender, Integer> {

    MALE(1),

    FEMALE(2);

    private Integer value;

    Gender(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
