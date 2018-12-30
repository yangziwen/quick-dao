package io.github.yangziwen.quickdao.core.util;

import io.github.yangziwen.quickdao.core.RepoKeys;
import lombok.Getter;

public class StringWrapper {

    @Getter
    private String prefix;

    @Getter
    private String suffix;

    public StringWrapper(String prefix, String suffix) {
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
