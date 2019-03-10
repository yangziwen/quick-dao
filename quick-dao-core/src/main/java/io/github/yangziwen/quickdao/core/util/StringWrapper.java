package io.github.yangziwen.quickdao.core.util;

import org.apache.commons.lang3.StringUtils;

import io.github.yangziwen.quickdao.core.RepoKeys;
import lombok.Getter;

public class StringWrapper {

    private static final StringWrapper EMPTY_WRAPPER = new StringWrapper("", "");

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

    public String unwrap(String value) {
        value = StringUtils.stripStart(value, prefix);
        value = StringUtils.stripEnd(value, suffix);
        return value;
    }

    public static StringWrapper emptyWrapper() {
        return EMPTY_WRAPPER;
    }

}
