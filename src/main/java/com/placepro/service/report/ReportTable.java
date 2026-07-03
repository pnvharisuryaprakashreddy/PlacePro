package com.placepro.service.report;

import java.util.Collections;
import java.util.List;

/**
 * Generic tabular report result: column headers plus data rows.
 * Kept generic so the same structure feeds the JTable, CSV, and PDF exports.
 */
public class ReportTable {

    private final String title;
    private final List<String> columns;
    private final List<List<Object>> rows;

    public ReportTable(String title, List<String> columns, List<List<Object>> rows) {
        this.title = title;
        this.columns = Collections.unmodifiableList(columns);
        this.rows = Collections.unmodifiableList(rows);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }
}
