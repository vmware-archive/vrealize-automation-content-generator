/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SerializationUtils {
    private static ObjectMapper jsonMapper;
    private static ObjectMapper yamlMapper;

    public static String toJson(Object o) throws JsonProcessingException {
        return getJsonMapper().writeValueAsString(o);
    }

    public static String toPrettyJson(Object o) throws JsonProcessingException {
        return getJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    public static <T> T fromJson(String o, T clazz) throws JsonProcessingException {
        return (T) getJsonMapper().readValue(o, clazz.getClass());
    }

    public static <T> T fromJson(String o, CollectionType type) throws JsonProcessingException {
        return (T) getJsonMapper().readValue(o, type);
    }

    public static <T> T fromYaml(String o, T clazz) throws JsonProcessingException {
        return (T) getYamlMapper().readValue(o, clazz.getClass());
    }

    public static String prettifyJson(String s) throws IOException {
        StringWriter writer = new StringWriter();
        getJsonMapper().writerWithDefaultPrettyPrinter()
                .writeValue(writer, jsonMapper.readValue(s, Object.class));
        return writer.toString();
    }

    public static String minimizeJson(String s) throws IOException {
        StringWriter writer = new StringWriter();
        getJsonMapper().writeValue(writer, jsonMapper.readValue(s, Object.class));
        return writer.toString();
    }

    public static String toYaml(Object o) throws JsonProcessingException {
        return getYamlMapper().writeValueAsString(o);
    }

    public static CollectionType getCollectionTypeOf(Class<? extends Collection> collection, Class clazz) {
        return getJsonMapper().getTypeFactory()
                .constructCollectionType(collection, clazz);
    }

    public static synchronized ObjectMapper getJsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = new ObjectMapper();
            jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonMapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
            jsonMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            jsonMapper.setSerializationInclusion(Include.NON_NULL);
        }

        return jsonMapper;
    }

    public static synchronized ObjectMapper getYamlMapper() {
        if (yamlMapper == null) {
            yamlMapper = new ObjectMapper(new YAMLFactory());
            yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            yamlMapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
            yamlMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            yamlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            yamlMapper.setSerializationInclusion(Include.NON_NULL);
        }

        return yamlMapper;
    }
}
