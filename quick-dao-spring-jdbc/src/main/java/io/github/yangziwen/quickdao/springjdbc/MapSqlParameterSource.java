package io.github.yangziwen.quickdao.springjdbc;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import io.github.yangziwen.quickdao.core.IEnum;

public class MapSqlParameterSource extends org.springframework.jdbc.core.namedparam.MapSqlParameterSource {

    public MapSqlParameterSource() {
    }

    public MapSqlParameterSource(String paramName, Object value) {
        super(paramName, value);
    }

    public MapSqlParameterSource(Map<String, ?> values) {
        super(values);
    }

    @Override
    public Object getValue(String paramName) {
        Object value = super.getValue(paramName);
        return ObjectUtils.defaultIfNull(IEnum.extractEnumValue(value), value);
    }

}
