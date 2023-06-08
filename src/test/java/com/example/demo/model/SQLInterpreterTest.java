package com.example.demo.model;

import com.example.demo.model.Column;
import com.example.demo.model.Table;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SQLInterpreterTest {

    @Test
    public void testSelect() {
        // Create a sample table with columns
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", "string"));
        columns.add(new Column("age", "int"));
        columns.add(new Column("salary", "double"));

        Table table = new Table("employees", columns);

        // Add some sample rows to the table
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("John", 25, 50000.0));
        rows.add(Arrays.asList("Alice", 30, 60000.0));
        rows.add(Arrays.asList("Bob", 35, 70000.0));

        table.addRows(rows);

        // Define the postfix tokens representing the RPN expression
        List<String> postfixTokens = Arrays.asList("name", "Alice", "=", "age", "30", "=", "OR");

        // Evaluate the RPN expression
        List<Map<String, Object>> result = SQLInterpreter.select(table, postfixTokens, null, 1);

        // Verify the result
        assertEquals(1, result.size());

        Map<String, Object> row = result.get(0);
        assertEquals("Alice", row.get("name"));
        assertEquals(30, row.get("age"));
        assertEquals(60000.0, row.get("salary"));

    }

    @Test
    public void testSelect2() {
        // Create a sample table with columns
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", "string"));
        columns.add(new Column("age", "int"));
        columns.add(new Column("salary", "double"));

        Table table = new Table("employees", columns);

        // Add some sample rows to the table
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("John", 25, 50000.0));
        rows.add(Arrays.asList("Alice", 30, 60000.0));
        rows.add(Arrays.asList("Bob", 35, 70000.0));

        table.addRows(rows);

        List<Map<String, Object>> result2 = SQLInterpreter.select(table, Arrays.asList("name", "Alice", "=", "age", "99", "<", "OR"), 2, null);

        // Verify the result
        System.out.println(result2);
        assertEquals(2, result2.size());

    }

    @Test
    public void testGetIndexes() {
        // Create a sample table with columns
        List<Column> columns = Arrays.asList(
                new Column("name", "string"),
                new Column("age", "int"),
                new Column("salary", "double")
        );

        Table table = new Table("employees", columns);

        // Add some sample rows to the table
        List<List<Object>> rows = Arrays.asList(
                Arrays.asList("John", 25, 50000.0),
                Arrays.asList("Alice", 30, 60000.0),
                Arrays.asList("Bob", 35, 70000.0)
        );

        table.addRows(rows);

        // Create a sample result list
        List<Map<String, Object>> results = Arrays.asList(
                Map.of("name", "Alice", "age", 30, "salary", 60000.0),
                Map.of("name", "Bob", "age", 35, "salary", 70000.0)
        );

        // Get the indexes of the rows in the original table
        List<Integer> indexes = SQLInterpreter.getIndexes(table, results);

        // Verify the expected indexes
        List<Integer> expectedIndexes = Arrays.asList(1, 2);
        assertEquals(expectedIndexes, indexes);
    }

    @Test
    public void testUpdate() {
        // Create a sample table with columns
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", "string"));
        columns.add(new Column("age", "int"));
        columns.add(new Column("salary", "double"));

        Table table = new Table("employees", columns);

        // Add some sample rows to the table
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("John", 25, 50000.0));
        rows.add(Arrays.asList("Alice", 30, 60000.0));
        rows.add(Arrays.asList("Bob", 35, 70000.0));
        table.addRows(rows);

        // Define the postfix tokens representing the update values and filter
        List<String> postfixValuesToUpdate = Arrays.asList("age", "=", "35");
        List<String> postfixFilter = Arrays.asList("name", "Alice", "=");

        // Perform the update
        List<Map<String, Object>> result = SQLInterpreter.update(table, postfixValuesToUpdate, postfixFilter);

        // Verify the updated values
        List<Map<String, Object>> expected = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "John");
        row1.put("age", 25);
        row1.put("salary", 50000.0);
        expected.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "Alice");
        row2.put("age", 35);
        row2.put("salary", 60000.0);
        expected.add(row2);
        Map<String, Object> row3 = new HashMap<>();
        row3.put("name", "Bob");
        row3.put("age", 35);
        row3.put("salary", 70000.0);
        expected.add(row3);

        assertEquals(expected, result);
    }

    @Test
    void testDelete() {
        // Create a sample table with columns
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("name", "string"));
        columns.add(new Column("age", "int"));
        columns.add(new Column("salary", "double"));

        Table table = new Table("employees", columns);

        // Add some sample rows to the table
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("John", 25, 50000.0));
        rows.add(Arrays.asList("Alice", 30, 60000.0));
        rows.add(Arrays.asList("Bob", 35, 70000.0));

        table.addRows(rows);

        // Define the postfix tokens representing the deletion condition
        List<String> postfixTokens = Arrays.asList("name", "Alice", "=");

        // Delete rows that match the condition
        SQLInterpreter.delete(table, postfixTokens);

        // Verify the rows have been deleted
        List<Map<String, Object>> remainingRows = table.listOfMap();
        assertEquals(2, remainingRows.size());

        // Verify the deleted row no longer exists
        for (Map<String, Object> row : remainingRows) {
            assertNotEquals("Alice", row.get("name"));
        }
    }

}
