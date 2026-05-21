package com.example.recyclescan3.model;

public class BinRule {
    public final String productType;
    public final Category category;
    public final String binLabel;

    public BinRule(String productType, Category category, String binLabel) {
        this.productType = productType;
        this.category = category;
        this.binLabel = binLabel;
    }
}
