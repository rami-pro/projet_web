package com.example.demo.model.request;

import java.util.List;

public class CreateTableRequest {
    private String tableName;
    private List<String> columnNames;
    private List<String> columnTypes;


    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getColumnTypes() {
        return columnTypes;
    }
}