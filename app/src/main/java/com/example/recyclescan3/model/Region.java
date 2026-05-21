package com.example.recyclescan3.model;

import java.util.List;

public class Region {
    public final String code;
    public final String displayName;
    public final List<BinRule> rules;

    public Region(String code, String displayName, List<BinRule> rules) {
        this.code = code;
        this.displayName = displayName;
        this.rules = rules;
    }
}
