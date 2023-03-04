package com.example.demo.controller;

import com.example.demo.TableService;
import com.example.demo.model.Column;
import com.example.demo.model.Table;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
public class UploadController {

    @Autowired
    private TableService tableService;

    @PostMapping("/upload")
    public void uploadCSV(@RequestParam("file") MultipartFile file, @RequestParam("tableName") String tableName) {
        try {
            InputStream inputStream = file.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<String> columnNames = new ArrayList<>();
            List<String> columnTypes = new ArrayList<>();
            List<Map<String, Object>> rows = new ArrayList<>();

            // Read the first line of the CSV file, which contains the column names and types.
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");
            for (String header : headers) {
                String[] parts = header.trim().split("\\s+");
                columnNames.add(parts[0]);
                columnTypes.add(parts[1]);
            }

            // Read the rest of the CSV file, which contains the data.
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    String columnType = columnTypes.get(i);
                    Object value = getValue(values[i], columnType);
                    row.put(columnName, value);
                }
                rows.add(row);
            }

            Table table = new Table(tableName, createColumns(columnNames, columnTypes), rows);
            tableService.addTable(table);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload CSV file", e);
        }
    }

    private List<Column> createColumns(List<String> columnNames, List<String> columnTypes) {
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String columnType = columnTypes.get(i);
            Column column = new Column(columnName, columnType);
            columns.add(column);
        }
        return columns;
    }

    private Object getValue(String value, String columnType) {
        if (columnType.equalsIgnoreCase("int")) {
            return Integer.parseInt(value);
        } else if (columnType.equalsIgnoreCase("double")) {
            return Double.parseDouble(value);
        } else if (columnType.equalsIgnoreCase("boolean")) {
            return Boolean.parseBoolean(value);
        } else {
            return value;
        }
    }
}