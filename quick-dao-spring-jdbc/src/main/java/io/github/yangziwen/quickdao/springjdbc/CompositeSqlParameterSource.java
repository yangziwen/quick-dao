package io.github.yangziwen.quickdao.springjdbc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class CompositeSqlParameterSource implements SqlParameterSource {

    private final List<SqlParameterSource> sources;

    public CompositeSqlParameterSource(SqlParameterSource...sources) {
        this(Arrays.asList(sources));
    }

    public CompositeSqlParameterSource(List<SqlParameterSource> sources) {
        this.sources = Collections.unmodifiableList(sources);
    }

    @Override
    public boolean hasValue(String paramName) {
        return sources.stream().anyMatch(source -> source.hasValue(paramName));
    }

    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        return sources.stream()
                .filter(source -> source.hasValue(paramName))
                .map(source -> source.getValue(paramName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getSqlType(String paramName) {
        return sources.stream()
                .filter(source -> source.hasValue(paramName))
                .map(source -> source.getSqlType(paramName))
                .filter(type -> type != TYPE_UNKNOWN)
                .findFirst()
                .orElse(TYPE_UNKNOWN);
    }

    @Override
    public String getTypeName(String paramName) {
        return sources.stream()
                .filter(source -> source.hasValue(paramName))
                .map(source -> source.getTypeName(paramName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }



}
