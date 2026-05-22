package com.example.recyclescan3.model;

public enum Category {
    RECYCLABLE("Recyclable"),
    COMPOST("Compost"),
    GENERAL_WASTE("General Waste"),
    HAZARDOUS("Hazardous");

    public final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
}
