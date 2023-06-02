package com.example.demo.model;

public class Column {

    static int i = 0;
    private String name;
    private String type;
    private int index;

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
        this.index = i++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }
}