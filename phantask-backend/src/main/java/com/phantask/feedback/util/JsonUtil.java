package com.phantask.feedback.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON write error", e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> ref) {
        try {
            return mapper.readValue(json, ref);
        } catch (Exception e) {
            throw new RuntimeException("JSON read error", e);
        }
    }
}
