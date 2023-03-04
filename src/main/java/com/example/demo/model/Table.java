package com.example.demo.model;

import java.util.*;
import java.util.stream.Collectors;

public class Table {

    private String name;
    private List<Column> columns;
    private List<Map<String, Object>> rows;

    public Table(String name, List<Column> columns, List<Map<String, Object>> rows) {
        this.name = name;
        this.columns = columns;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<String> getColumnNames() {
        return columns.stream().map(Column::getName).collect(Collectors.toList());
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}