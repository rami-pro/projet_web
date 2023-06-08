package com.example.demo.model.request;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class UpdateRequest {
    private String tableName;
    private String filter;
    private String updateStatement;

    public String getTableName() {
        return tableName;
    }

    public String getFilter() {
        return filter;
    }


    public String getUpdateStatement() {
        return updateStatement;
    }
}