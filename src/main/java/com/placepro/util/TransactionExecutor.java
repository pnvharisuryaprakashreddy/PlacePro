package com.placepro.util;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static <T> T run(TransactionCallback<T> callback) {
        return execute(callback);
    }

    public static <T> T execute(TransactionCallback<T> callback) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = callback.execute(connection);
                connection.commit();
                return result;
            } catch (RuntimeException exception) {
                rollbackQuietly(connection);
                throw exception;
            } catch (SQLException exception) {
                rollbackQuietly(connection);
                AppLog.sqlError("TransactionExecutor.execute", exception);
                com.placepro.monitoring.MetricsRegistry.get().recordError("service");
                throw new DatabaseConnectionException("Failed to complete database transaction.", exception);
            }
        } catch (SQLException exception) {
            AppLog.sqlError("TransactionExecutor.getConnection", exception);
            com.placepro.monitoring.MetricsRegistry.get().recordError("service");
            throw new DatabaseConnectionException("Failed to start database transaction.", exception);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            System.err.println("Failed to roll back transaction: " + exception.getMessage());
        }
    }
}
