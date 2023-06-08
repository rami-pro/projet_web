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
    public void uploadCSV(@RequestParam("file") MultipartFile file,
                          @RequestParam("tableName") String tableName,
                          @RequestParam("skip") long skip) {
        Table table = tableService.getTableByName(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table " + tableName + " doesn't exist");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<List<Object>> rows = new ArrayList<>();

            // Read the rest of the CSV file, which contains the data.
            String line;
            List<Column> columns = table.getColumns();
            while ((line = reader.readLine()) != null) {
                if (--skip >= 0)
                    continue;

                String[] values = line.split(",");
                List<Object> row = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    Column column = columns.get(i);
                    Object value = getValue(values[i], column.getType());
                    row.add(value);
                }
                rows.add(row);
            }
            table.addRows(rows);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload CSV file", e);
        }
    }

    private Object getValue(String value, String columnType) {
        if (columnType.equalsIgnoreCase("int")) {
            return Integer.parseInt(value);
        } else if (columnType.equalsIgnoreCase("double")) {
            return Double.parseDouble(value);
        } else if (columnType.equalsIgnoreCase("boolean")) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }
}