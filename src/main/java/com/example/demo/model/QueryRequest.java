package com.example.demo.model;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class QueryRequest {
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTableName() {
        Pattern pattern = Pattern.compile("FROM\\s+(\\w+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Invalid SELECT query: " + query);
        }
    }

    public List<String> getColumns(List<String> allColumns) {
        Pattern pattern = Pattern.compile("SELECT\\s+(.*)\\s+FROM");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String columnString = matcher.group(1);
            if(columnString.trim().equals("*")) {
                return allColumns;
            }
            String[] columns = columnString.split(",");
            return Arrays.stream(columns)
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Invalid SELECT query: " + query);
        }
    }
}