package com.example.demo;

import com.example.demo.model.Table;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TableService {

    private List<Table> tables = new ArrayList<>();

    public void addTable(Table table) {
        tables.add(table);
    }

    public List<Table> getAllTables() {
        return tables;
    }

    public Table getTableByName(String tableName) {
        for (Table table : tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    public List<Map<String, Object>> executeQuery(Table table, List<String> columns) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get the list of rows from the table
        List<Map<String, Object>> rows = table.getRows();

        // Loop through each row
        for (Map<String, Object> row : rows) {
            Map<String, Object> filteredRow = new HashMap<>();
            // Loop through each column in the row
            for (String column : columns) {
                // If the column exists in the row, add it to the filtered row
                if (row.containsKey(column)) {
                    filteredRow.put(column, row.get(column));
                }
            }
            // If the filtered row has at least one column, add it to the result
            if (!filteredRow.isEmpty()) {
                result.add(filteredRow);
            }
        }

        return result;
    }
}