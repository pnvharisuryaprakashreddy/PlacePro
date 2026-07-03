package com.placepro.dao.impl;

import com.placepro.dao.DataAccessException;
import com.placepro.util.AppLog;
import com.placepro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

abstract class AbstractJdbcDAO {

    protected Connection getConnection() {
        return DBConnection.getConnection();
    }

    protected DataAccessException translateException(String operation, SQLException exception) {
        AppLog.sqlError(operation, exception);
        com.placepro.monitoring.MetricsRegistry.get().recordError("dao");
        return new DataAccessException("A database error occurred while processing your request.", exception);
    }

    protected int executeUpdate(PreparedStatement statement, String operation) throws SQLException {
        try {
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw translateException(operation, exception);
        }
    }
}
