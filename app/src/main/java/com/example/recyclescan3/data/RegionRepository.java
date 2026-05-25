package com.example.recyclescan3.data;

import com.example.recyclescan3.model.BinRule;
import com.example.recyclescan3.WasteCategory;
import com.example.recyclescan3.model.Region;

import java.util.Arrays;
import java.util.List;

public class RegionRepository {

    private static final List<Region> REGIONS = Arrays.asList(
            new Region("paris", "Paris", Arrays.asList(
                    new BinRule("Plastic bottles",    WasteCategory.RECYCLABLE,    "Yellow bin"),
                    new BinRule("Cardboard / paper",  WasteCategory.RECYCLABLE,    "Yellow bin"),
                    new BinRule("Glass",              WasteCategory.RECYCLABLE,    "Green bin"),
                    new BinRule("Food scraps",        WasteCategory.COMPOST,       "Brown bin"),
                    new BinRule("Mixed plastic bags", WasteCategory.GENERAL_WASTE, "Gray bin"),
                    new BinRule("Batteries",          WasteCategory.HAZARDOUS,     "Hazardous drop-off"),
                    new BinRule("Electronics",        WasteCategory.HAZARDOUS,     "WEEE drop-off")
            )),
            new Region("lyon", "Lyon", Arrays.asList(
                    new BinRule("Plastic bottles",    WasteCategory.RECYCLABLE,    "Yellow bin"),
                    new BinRule("Cardboard / paper",  WasteCategory.RECYCLABLE,    "Yellow bin"),
                    new BinRule("Glass",              WasteCategory.RECYCLABLE,    "Green bin"),
                    new BinRule("Food scraps",        WasteCategory.COMPOST,       "Brown bin"),
                    new BinRule("Mixed plastic bags", WasteCategory.RECYCLABLE,    "Yellow bin"),
                    new BinRule("Batteries",          WasteCategory.HAZARDOUS,     "Hazardous drop-off"),
                    new BinRule("Electronics",        WasteCategory.HAZARDOUS,     "WEEE drop-off")
            )),
            new Region("marseille", "Marseille", Arrays.asList(
                    new BinRule("Plastic bottles",    WasteCategory.RECYCLABLE,    "Blue bin"),
                    new BinRule("Cardboard / paper",  WasteCategory.RECYCLABLE,    "Blue bin"),
                    new BinRule("Glass",              WasteCategory.RECYCLABLE,    "Green bin"),
                    new BinRule("Food scraps",        WasteCategory.GENERAL_WASTE, "Gray bin"),
                    new BinRule("Mixed plastic bags", WasteCategory.GENERAL_WASTE, "Gray bin"),
                    new BinRule("Batteries",          WasteCategory.HAZARDOUS,     "Hazardous drop-off"),
                    new BinRule("Electronics",        WasteCategory.HAZARDOUS,     "WEEE drop-off")
            ))
    );

    public static List<Region> getAll() {
        return REGIONS;
    }

    public static Region getByCode(String code) {
        for (Region r : REGIONS) {
            if (r.code.equals(code)) return r;
        }
        return REGIONS.get(0);
    }

    public static Region getDefault() {
        return REGIONS.get(0);
    }
}