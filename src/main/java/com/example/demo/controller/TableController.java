package com.example.demo.controller;

import com.example.demo.TableService;
import com.example.demo.model.Column;
import com.example.demo.model.request.CreateTableRequest;
import com.example.demo.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TableController {

    @Autowired
    private TableService tableService;

    @PostMapping("/createTable")
    public Table createTable(@RequestBody CreateTableRequest body) {
        if (tableService.isTable(body.getTableName())) {
            throw new IllegalArgumentException("Table " + body.getTableName() + " already exist in DB");
        }
        List<Column> columns = createColumns(body.getColumnNames(), body.getColumnTypes());
        Table table = new Table(body.getTableName(), columns);
        tableService.addTable(table);
        return table;
    }

    private List<Column> createColumns(List<String> columnNames, List<String> columnTypes) {
        List<Column> columns = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String columnType = columnTypes.get(i);
            Column column = new Column(columnName, columnType);
            columns.add(column);
        }
        return columns;
    }

}