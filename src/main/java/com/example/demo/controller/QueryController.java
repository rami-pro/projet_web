package com.example.demo.controller;

import com.example.demo.model.request.DeleteRequest;
import com.example.demo.model.request.QueryRequest;
import com.example.demo.TableService;
import com.example.demo.model.SQLInterpreter;
import com.example.demo.model.Table;
import com.example.demo.model.request.SelectRequest;
import com.example.demo.model.request.UpdateRequest;
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

        return SQLInterpreter.executeQuery(table, columns, queryRequest.getWhereClause(), queryRequest.getVerb(), queryRequest.getQuery());
    }

    @PostMapping("/select")
    public List<Map<String, Object>> select(@RequestBody SelectRequest selectRequestBody) {
        String tableName = selectRequestBody.getTableName();
        Table table = tableService.getTableByName(tableName);

        if (table == null) {
            return new ArrayList<>();
        }



        List<String> columns = selectRequestBody.getColumns();
        //return SQLInterpreter.executeQuery(table, columns, queryRequest.getWhereClause(), queryRequest.getVerb(), queryRequest.getQuery());
        return SQLInterpreter.executeSelectQuery(table, columns, selectRequestBody.getFilter(), selectRequestBody.getLimit(), selectRequestBody.getSkip());
    }

    @PostMapping("/update")
    public List<Map<String, Object>> update(@RequestBody UpdateRequest updateBody) {
        String tableName = updateBody.getTableName();
        Table table = tableService.getTableByName(tableName);

        if (table == null) {
            return new ArrayList<>();
        }

        return SQLInterpreter.executeUpdateQuery(table, updateBody.getFilter(), updateBody.getUpdateStatement());
    }

    @PostMapping("/delete")
    public List<Map<String, Object>> delete(@RequestBody DeleteRequest deleteRequest) {
        String tableName = deleteRequest.getTableName();
        Table table = tableService.getTableByName(tableName);

        if (table == null) {
            return new ArrayList<>();
        }

        return SQLInterpreter.executeDeleteQuery(table,deleteRequest.getFilter());
    }
}
