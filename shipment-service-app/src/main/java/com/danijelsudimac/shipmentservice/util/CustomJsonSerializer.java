package com.danijelsudimac.shipmentservice.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public final class CustomJsonSerializer implements Serializer<Object> {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    static {
        OBJECT_MAPPER.addMixIn(
                org.apache.avro.specific.SpecificRecordBase.class,
                AvroMixin.class
        );
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Object o) {
        return serialize(o);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Object data) {
        return serialize(data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }

    @JsonIgnoreProperties({"specificData", "schema"})
    abstract static class AvroMixin {
    }

    public static byte[] serialize(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Failed to serialize object of type: "
                            + object.getClass().getSimpleName(),
                    e
            );
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(data, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to deserialize object to type: "
                            + clazz.getSimpleName(),
                    e
            );
        }
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Failed to convert object to JSON",
                    e
            );
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to parse JSON to type: "
                            + clazz.getSimpleName(),
                    e
            );
        }
    }
}

