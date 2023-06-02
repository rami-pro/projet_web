package com.example.demo.model;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLInterpreter {
    public static List<Map<String, Object>> executeQuery(Table table, List<String> columns, String whereClause) {
        // Tokenize the WHERE clause
        List<String> tokens = tokenize(whereClause);

        System.out.println(tokens);
        // Convert to RPN
        List<String> postfixTokens = convertToRPN(tokens);

        // Evaluate RPN expression
        List<Map<String, Object>> result = evaluateRPN(table, postfixTokens);


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

    public static List<Map<String, Object>> evaluateRPN(Table table, List<String> postfixTokens) {
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