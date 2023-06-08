package com.example.demo.model.request;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class DeleteRequest {
    private String tableName;
    private String filter;
    private List<String> columns;

    public String getTableName() {
        return tableName;
    }

    public String getFilter() {
        return filter;
    }

    public List<String> getColumns() {
        return columns;
    }
}