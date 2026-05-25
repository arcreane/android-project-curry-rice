package com.example.recyclescan3.model;

import com.example.recyclescan3.WasteCategory;

public class BinRule {
    public final String productType;
    public final WasteCategory category;
    public final String binLabel;

    public BinRule(String productType, WasteCategory category, String binLabel) {
        this.productType = productType;
        this.category = category;
        this.binLabel = binLabel;
    }
}