package com.example.demo.controller;

import com.example.demo.model.QueryRequest;
import com.example.demo.TableService;
import com.example.demo.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class QueryController {

    @Autowired
    private TableService tableService;

    @PostMapping("/query")
    public List<Map<String, Object>> queryTable(@RequestBody QueryRequest queryRequest) {
        String tableName = queryRequest.getTableName();
        Table table = tableService.getTableByName(tableName);

        if (table == null) {
            //it would be better to send an exception
            return new ArrayList<>();
        }

        List<String> columns = queryRequest.getColumns(table.getColumnNames());
        List<Map<String, Object>> result = tableService.executeQuery(table, columns);

        return result;
    }
}
