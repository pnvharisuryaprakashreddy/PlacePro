package com.placepro.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class DBConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    static {
        loadDriver();
    }

    private DBConnection() {
    }

    public static Connection getConnection() {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return DriverManager.getConnection(
                        getRequiredProperty("db.url"),
                        getRequiredProperty("db.user"),
                        getPassword());
            } catch (SQLException exception) {
                lastException = exception;
                AppLog.sqlError("DBConnection.getConnection attempt=" + attempt, exception);
            }
        }

        com.placepro.monitoring.MetricsRegistry.get().recordError("dao");
        throw new DatabaseConnectionException(
                "Unable to connect to the database after 2 attempts. Check config.properties and ensure MySQL is running.",
                lastException);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new DatabaseConnectionException(
                        "Could not find " + CONFIG_FILE + " on the classpath. Create src/main/resources/" + CONFIG_FILE + ".",
                        null);
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new DatabaseConnectionException("Failed to load database configuration from " + CONFIG_FILE + ".", exception);
        }
    }

    private static String getRequiredProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            throw new DatabaseConnectionException("Missing required database property: " + key, null);
        }
        return value.trim();
    }

    private static String getPassword() {
        String value = PROPERTIES.getProperty("db.password");
        return Objects.isNull(value) ? "" : value;
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new DatabaseConnectionException("MySQL JDBC driver not found on the classpath.", exception);
        }
    }
}
