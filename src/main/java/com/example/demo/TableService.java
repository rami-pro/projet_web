package com.example.demo;

import com.example.demo.model.Table;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TableService {

    private final Map<String, Table> tables = new HashMap<>();

    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public List<Table> getAllTables() {
        return new ArrayList<>(tables.values());
    }

    public Table getTableByName(String tableName) {
        return tables.get(tableName);
    }

    public boolean isTable(String tableName) {
        return tables.containsKey(tableName);
    }

}