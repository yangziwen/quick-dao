package io.github.yangziwen.quickdao.core;

import lombok.Getter;

public class PlaceholderWrapper {

    @Getter
    private String prefix;

    @Getter
    private String suffix;

    public PlaceholderWrapper(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String wrap(Object value) {
        return prefix + value + suffix;
    }

    public String wrap(int index, Object value) {
        return prefix + value + RepoKeys.__ + index + suffix;
    }

}
