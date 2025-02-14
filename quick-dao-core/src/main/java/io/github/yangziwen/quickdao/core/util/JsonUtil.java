package io.github.yangziwen.quickdao.core.util;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() { };

    static {

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);

    }

    public static String serialize(Object data) {
        try {
            return null == data ? "" : mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to serialize data to json", e);
        }
    }

    public static <T> T deserialize(String json, TypeReference<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("failed to deserialize json to data", e);
        }
    }

    public static Map<String, Object> deserializeToMap(String json) {
        Map<String, Object> resultMap = deserialize(json, MAP_TYPE_REFERENCE);
        if (resultMap == null) {
            return new HashMap<>();
        } else {
            return resultMap;
        }
    }

    public static <T> T deserialize(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("failed to deserialize json to data", e);
        }
    }

}