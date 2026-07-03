package com.placepro.service.student;

import com.placepro.dao.StudentSearchRow;

import java.util.Collections;
import java.util.List;

/**
 * One page of student search results plus the total match count for pagination.
 */
public class StudentSearchPage {

    private final List<StudentSearchRow> rows;
    private final int totalCount;
    private final int page;
    private final int pageSize;

    public StudentSearchPage(List<StudentSearchRow> rows, int totalCount, int page, int pageSize) {
        this.rows = Collections.unmodifiableList(rows);
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<StudentSearchRow> getRows() {
        return rows;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalCount == 0 ? 1 : (totalCount + pageSize - 1) / pageSize;
    }
}
