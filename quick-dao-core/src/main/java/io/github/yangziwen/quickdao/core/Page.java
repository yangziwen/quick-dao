package io.github.yangziwen.quickdao.core;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Page<E> {

    private int pageNo;

    private int pageSize;

    private List<E> list;

    private Integer totalCount;

    public Page(int pageNo, int pageSize, List<E> list, Integer totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.list = list;
        this.totalCount = totalCount;
    }

}
