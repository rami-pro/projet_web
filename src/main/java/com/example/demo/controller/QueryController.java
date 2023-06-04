package com.example.demo.controller;

import com.example.demo.model.request.QueryRequest;
import com.example.demo.TableService;
import com.example.demo.model.SQLInterpreter;
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
            return new ArrayList<>();
        }

        List<String> columns = queryRequest.getColumns(table.getColumnNames());

        return SQLInterpreter.executeQuery(table, columns, queryRequest.getWhereClause(), queryRequest.getVerb());
    }
}
