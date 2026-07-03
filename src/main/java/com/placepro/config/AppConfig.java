package com.placepro.config;

import com.placepro.util.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppConfig() {
    }

    public static String getProperty(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value.trim();
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    public static int getResumesMaxSizeKb() {
        return getIntProperty("resumes.maxSizeKb", 2048);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new DatabaseConnectionException(
                        "Could not find " + CONFIG_FILE + " on the classpath.",
                        null);
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new DatabaseConnectionException("Failed to load application configuration.", exception);
        }
    }
}
