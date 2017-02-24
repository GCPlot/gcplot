package com.gcplot.commons.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gcplot.commons.exceptions.Exceptions;

import java.io.IOException;
import java.util.List;

public abstract class JsonSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        //mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disableDefaultTyping();
    }

    public static String serialize(Object msg) {
        try {
            return mapper.writer().writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw Exceptions.runtime(e);
        }
    }

    public static <U> U deserialize(String input, Class<? extends U> type) {
        try {
            return mapper.readValue(input, type);
        } catch (IOException e) {
            throw Exceptions.runtime(e);
        }
    }

    public static <U> List<U> deserializeList(String input, Class<? extends U> type) {
        try {
            return mapper.readValue(input, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw Exceptions.runtime(e);
        }
    }
}
