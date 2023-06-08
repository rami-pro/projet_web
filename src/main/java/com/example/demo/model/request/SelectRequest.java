package com.example.demo.model.request;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class SelectRequest {
    private String tableName;
    private String filter;
    private Integer skip;
    private Integer limit;
    private List<String> columns;

    public String getTableName() {
        return tableName;
    }

    public String getFilter() {
        if(filter == null) {
            return "";
        }
        return filter;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Integer getSkip() {
        return skip;
    }

    public Integer getLimit() {
        return limit;
    }

    //private List<String> aggretate;


}