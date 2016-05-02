package com.gcplot.commons.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gcplot.commons.exceptions.Exceptions;

import java.io.IOException;

public abstract class JsonSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY);
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
}
