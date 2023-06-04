package com.example.demo.model;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SQLInterpreter {
    public static List<Map<String, Object>> executeQuery(Table table, List<String> columns, String whereClause, String verb, String query) {
        // Tokenize the WHERE clause
        List<String> tokens = tokenize(whereClause);

        System.out.println(tokens);
        // Convert to RPN
        List<String> postfixTokens = convertToRPN(tokens);

        // Evaluate RPN expression
        List<Map<String, Object>> result = evaluateRPN(table, postfixTokens, verb, query);


        return projectColumns(result, columns);
    }

    private static List<String> tokenize(String whereClause) {
        List<String> tokens = new ArrayList<>();

        String[] operators = {"=", ">=", "<=", "!=", "<", ">", "AND", "OR"};
        String regex = String.format("(%s)|\\s+|([()]|[^\\s()]+)", String.join("|", operators));
        Matcher matcher = Pattern.compile(regex).matcher(whereClause);

        while (matcher.find()) {
            String token = matcher.group().trim();
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        return tokens;
    }


    private static List<String> convertToRPN(List<String> infixTokens) {
        List<String> outputQueue = new ArrayList<>();
        Deque<String> operatorStack = new ArrayDeque<>();

        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("=", 1);
        precedence.put(">=", 1);
        precedence.put("<=", 1);
        precedence.put("!=", 1);
        precedence.put("<", 1);
        precedence.put(">", 1);
        precedence.put("AND", 2);
        precedence.put("OR", 3);
        precedence.put("(", 4);
        precedence.put(")", 4);

        for (String token : infixTokens) {
            if (isOperator(token)) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(") &&
                        precedence.get(operatorStack.peek()) >= precedence.get(token)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.pop(); // Discard the opening parenthesis
            } else {
                outputQueue.add(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }

        System.out.println(outputQueue);

        return outputQueue;
    }

    public static List<Map<String, Object>> projectColumns(List<Map<String, Object>> rows, List<String> columns) {
        List<Map<String, Object>> projectedRows = new ArrayList<>();

        if(columns == null) {
            return rows;
        }

        for (Map<String, Object> row : rows) {
            Map<String, Object> projectedRow = new HashMap<>();

            for (String column : columns) {
                if (row.containsKey(column)) {
                    projectedRow.put(column, row.get(column));
                }
            }

            projectedRows.add(projectedRow);
        }

        return projectedRows;
    }

    public static List<String> getColumnsAndValues(String updateStatement) {
        List<String> columnsAndValues = new ArrayList<>();

        // Regular expression pattern to match column-value pairs
        Pattern pattern = Pattern.compile("SET\\s+(.+?)\\s+WHERE");
        Matcher matcher = pattern.matcher(updateStatement);

        if (matcher.find()) {
            String columnValuePairs = matcher.group(1);

            // Split the column-value pairs by commas
            String[] pairs = columnValuePairs.split(",");

            // Process each pair
            for (String pair : pairs) {
                // Remove leading/trailing whitespace
                pair = pair.trim();

                // Split the pair into column and value
                String[] parts = pair.split("=");

                // Remove leading/trailing whitespace from column and value
                String column = parts[0].trim();
                String value = parts[1].trim();

                // Add the column and value to the list
                columnsAndValues.add(column);
                columnsAndValues.add("=");
                columnsAndValues.add(value);
            }
        } else {
            throw new IllegalArgumentException("Invalid SQL update statement: " + updateStatement);
        }

        return columnsAndValues;
    }



    public static List<Map<String, Object>> evaluateRPN(Table table, List<String> postfixTokens, String action, String query) {
        switch (action) {
            case "SELECT":
                return select(table, postfixTokens);
            case "UPDATE":
                //UPDATE Table SET column1 = value1, column2 = value2 WHERE id = 1 ==> [column1, value1, column2, =, value2]
                List<String> columnValuesToUpdate = getColumnsAndValues(query);
                return update(table, columnValuesToUpdate, postfixTokens);
            case "DELETE":
                return delete(table, postfixTokens);
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }


    public static List<Integer> getIndexes(Table table, List<Map<String, Object>> results) {
        List<List<Object>> rows = table.getRows();

        return IntStream.range(0, rows.size())
                .filter(i -> results.stream().anyMatch(result -> table.compareRowWithMap(rows.get(i), result)))
                .boxed()
                .collect(Collectors.toList());
    }


    public static List<Map<String, Object>> select(Table table, List<String> postfixTokens) {
        Stack<List<Map<String, Object>>> stack = new Stack<>();
        Stack<String> operands = new Stack<>();

        if(postfixTokens.isEmpty()) {
            return table.listOfMap();
        }

        for (String token : postfixTokens) {
            if (isOperator(token)) {
                if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                    List<Map<String, Object>> rightOperand = stack.pop();
                    List<Map<String, Object>> leftOperand = stack.pop();
                    List<Map<String, Object>> result = applyLogicalOperator(leftOperand, token, rightOperand);
                    stack.push(result);
                } else {
                    String rightOperand = operands.pop();
                    String leftOperand = operands.pop();
                    List<Map<String, Object>> result = applyOperator(table, leftOperand, token, rightOperand);
                    stack.push(result);
                }
            } else {
                operands.push(token);
            }
        }

        return stack.peek();
    }

    public static List<Map<String, Object>> update(Table table, List<String> postfixValuesToUpdate, List<String> postfixFilter) {
        List<Map<String, Object>> rowsToUpdate = select(table, postfixFilter);
        List<Integer> rowsIndex = getIndexes(table, rowsToUpdate);

        for (int i = 0; i < rowsToUpdate.size(); i++) {
            for (int j = 0; j < postfixValuesToUpdate.size(); j += 3) {
                String columnName = postfixValuesToUpdate.get(j);
                String operator = postfixValuesToUpdate.get(j + 1);
                String value = postfixValuesToUpdate.get(j + 2);

                // Find the column in the table
                Column column = table.getColumns(List.of(columnName)).get(0);

                // Parse the value according to the column type
                Object parsedValue = table.parseValue(value, column.getType());

                // Update the value in the row
                rowsToUpdate.get(i).put(columnName, parsedValue);
            }
            table.updateRowWithMap(rowsIndex.get(i), rowsToUpdate.get(i));
        }


        // Apply the update values to the selected rows
        for (Map<String, Object> row : rowsToUpdate) {
            for (int i = 0; i < postfixValuesToUpdate.size(); i += 3) {
                String columnName = postfixValuesToUpdate.get(i);
                String operator = postfixValuesToUpdate.get(i + 1);
                String value = postfixValuesToUpdate.get(i + 2);

                // Find the column in the table
                Column column = table.getColumns(List.of(columnName)).get(0);

                // Parse the value according to the column type
                Object parsedValue = table.parseValue(value, column.getType());

                // Update the value in the row
                row.put(columnName, parsedValue);
            }
        }

        return table.listOfMap();
    }

    public static List<Map<String, Object>> delete(Table table, List<String> postfixTokens) {
        // Evaluate the postfix tokens to get the filtered results
        List<Map<String, Object>> results = select(table, postfixTokens);

        // Get the indexes of the rows to delete
        List<Integer> indexes = getIndexes(table, results);

        // Delete the rows
        table.deleteRows(indexes);

        // Return the updated rows
        return results;
    }



    public static List<Map<String, Object>> applyOperator(Table table, String leftOperand, String operator, String rightOperand) {
        List<Map<String, Object>> result = new ArrayList<>();

        Column column = table.getColumns(List.of(leftOperand)).get(0);
        Object value = table.parseValue(rightOperand, column.getType());

        Predicate<List<Object>> condition = row -> table.compareValues(row.get(column.getIndex()), value, operator);

        for (List<Object> row : table.getRows()) {
            if (condition.test(row)) {
                Map<String, Object> filteredRow = new HashMap<>();
                for (Column col : table.getColumns()) {
                    filteredRow.put(col.getName(), row.get(col.getIndex()));
                }
                result.add(filteredRow);
            }
        }

        return result;
    }

    public static List<Map<String, Object>> applyLogicalOperator(List<Map<String, Object>> leftOperand, String operator, List<Map<String, Object>> rightOperand) {
        switch (operator.toUpperCase()) {
            case "AND":
                return leftOperand.stream().filter(rightOperand::contains).toList();
            case "OR":
                return Stream.concat(leftOperand.stream(), rightOperand.stream())
                        .distinct()
                        .toList();
            default:
                throw new IllegalArgumentException("Invalid logical operator: " + operator);
        }
    }

    private static boolean isOperator(String token) {
        List<String> operators = List.of("=", ">=", "<=", "!=", "<", ">", "AND", "OR");
        return operators.contains(token.toUpperCase());
    }

}