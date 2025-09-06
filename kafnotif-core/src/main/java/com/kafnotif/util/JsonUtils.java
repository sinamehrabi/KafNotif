package com.kafnotif.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for JSON serialization/deserialization with proper Java 8+ date/time support
 */
public class JsonUtils {
    
    /**
     * Creates a pre-configured ObjectMapper with JSR310 module for Java 8+ date/time types
     * and configured to ignore unknown properties for better compatibility
     * @return configured ObjectMapper instance
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        // Configure to ignore unknown properties for better compatibility
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }
    
    private JsonUtils() {
        // Utility class - prevent instantiation
    }
}
