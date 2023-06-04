package com.example.demo.model;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Table {

    private final String name;
    private final List<Column> columns;
    private final List<List<Object>> rows = new ArrayList<>();

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns(List<String> cols) {
        List<Column> filteredColumns = new ArrayList<>();

        for (String colName : cols) {
            for (Column column : this.columns) {
                if (column.getName().equals(colName)) {
                    filteredColumns.add(column);
                    break;
                }
            }
        }

        return filteredColumns;
    }
    public List<Column> getColumns() {
        return columns;
    }



    public List<String> getColumnNames() {
        return columns.stream().map(Column::getName).collect(Collectors.toList());
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public void addRows(List<List<Object>> rows) {
        this.rows.addAll(rows);
    }
    public void addRow(List<Object> row) {
        this.rows.add(row);
    }

    private List<Map<String, Object>> executeQuery(Table table, List<String> columns) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get the list of rows from the table
        List<List<Object>> rows = table.getRows();
        List<Column> filteredColumns = table.getColumns(columns);

        // Iterate over each row and create a new map containing only the filtered columns
        for (List<Object> row : rows) {
            Map<String, Object> filteredRow = new HashMap<>();
            for (Column column : filteredColumns) {
                filteredRow.put(column.getName(), row.get(column.getIndex()));
            }
            result.add(filteredRow);
        }

        return result;
    }

    public List<Map<String, Object>> listOfMap() {
        return executeQuery(this, this.getColumnNames());
    }

    public Object parseValue(String token, String columnType) {
        if (columnType.equalsIgnoreCase("int")) {
            return Integer.parseInt(token);
        } else if (columnType.equalsIgnoreCase("double")) {
            return Double.parseDouble(token);
        } else if (columnType.equalsIgnoreCase("boolean")) {
            return Boolean.parseBoolean(token);
        } else {
            return token;
        }
    }

    public static boolean compareValues(Object value1, Object value2, String operator) {
        if (value1 == null || value2 == null) {
            return false;
        }
        int compareResult;
        if (value1 instanceof String && value2 instanceof String) {
            compareResult = ((String) value1).compareTo((String) value2);
        } else if (value1 instanceof Number && value2 instanceof Number) {
            double doubleValue1 = ((Number) value1).doubleValue();
            double doubleValue2 = ((Number) value2).doubleValue();
            compareResult = Double.compare(doubleValue1, doubleValue2);
        } else if (value1 instanceof Boolean && value2 instanceof Boolean) {
            compareResult = ((Boolean) value1).compareTo((Boolean) value2);
        } else {
            return false; // can't compare other types
        }
        switch (operator) {
            case "=":
                return compareResult == 0;
            case "!=":
                return compareResult != 0;
            case ">":
                return compareResult > 0;
            case ">=":
                return compareResult >= 0;
            case "<":
                return compareResult < 0;
            case "<=":
                return compareResult <= 0;
            default:
                return false;
        }
    }

    public void deleteRows(List<Integer> indexes) {
        // Sort the indexes in descending order to ensure correct row deletion
        Collections.sort(indexes, Collections.reverseOrder());

        // Remove the rows at the specified indexes
        for (int index : indexes) {
            if (index >= 0 && index < rows.size()) {
                rows.remove(index);
            }
        }
    }


    public Map<String, Object> convertRowToMap(Table table, List<Object> row) {
        List<Column> columns = table.getColumns();
        List<String> columnNames = table.getColumnNames();

        Map<String, Object> map = new HashMap<>();

        for (Column column : columns) {
            int columnIndex = column.getIndex();
            String columnName = columnNames.get(columnIndex);
            Object value = row.get(columnIndex);
            map.put(columnName, value);
        }

        return map;
    }

    public boolean compareRowWithMap(List<Object> row, Map<String, Object> map) {
        for (Column column : columns) {
            String columnName = column.getName();
            Object rowValue = row.get(column.getIndex());
            Object mapValue = map.get(columnName);
            String columnType = column.getType();

            Object parsedRowValue = parseValue(rowValue.toString(), columnType);
            Object parsedMapValue = parseValue(mapValue.toString(), columnType);

            if (!compareValues(parsedRowValue, parsedMapValue, "=")) {
                return false;
            }
        }

        return true;
    }

    public void updateRowWithMap(int rowIndex, Map<String, Object> map) {
        List<Object> row = rows.get(rowIndex);

        for (Column column : columns) {
            String columnName = column.getName();
            Object value = map.get(columnName);

            // Update the value in the row
            row.set(column.getIndex(), value);
        }
    }


}