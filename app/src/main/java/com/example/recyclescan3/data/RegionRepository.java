package com.example.recyclescan3.data;

import com.example.recyclescan3.model.BinRule;
import com.example.recyclescan3.model.Category;
import com.example.recyclescan3.model.Region;

import java.util.Arrays;
import java.util.List;

public class RegionRepository {

    private static final List<Region> REGIONS = Arrays.asList(
        new Region("paris", "Paris", Arrays.asList(
            new BinRule("Plastic bottles", Category.RECYCLABLE, "Yellow bin"),
            new BinRule("Cardboard / paper", Category.RECYCLABLE, "Yellow bin"),
            new BinRule("Glass", Category.RECYCLABLE, "Green bin"),
            new BinRule("Food scraps", Category.COMPOST, "Brown bin"),
            new BinRule("Mixed plastic bags", Category.GENERAL_WASTE, "Gray bin"),
            new BinRule("Batteries", Category.HAZARDOUS, "Hazardous drop-off"),
            new BinRule("Electronics", Category.HAZARDOUS, "WEEE drop-off")
        )),
        new Region("lyon", "Lyon", Arrays.asList(
            new BinRule("Plastic bottles", Category.RECYCLABLE, "Yellow bin"),
            new BinRule("Cardboard / paper", Category.RECYCLABLE, "Yellow bin"),
            new BinRule("Glass", Category.RECYCLABLE, "Green bin"),
            new BinRule("Food scraps", Category.COMPOST, "Brown bin"),
            new BinRule("Mixed plastic bags", Category.RECYCLABLE, "Yellow bin"),
            new BinRule("Batteries", Category.HAZARDOUS, "Hazardous drop-off"),
            new BinRule("Electronics", Category.HAZARDOUS, "WEEE drop-off")
        )),
        new Region("marseille", "Marseille", Arrays.asList(
            new BinRule("Plastic bottles", Category.RECYCLABLE, "Blue bin"),
            new BinRule("Cardboard / paper", Category.RECYCLABLE, "Blue bin"),
            new BinRule("Glass", Category.RECYCLABLE, "Green bin"),
            new BinRule("Food scraps", Category.GENERAL_WASTE, "Gray bin"),
            new BinRule("Mixed plastic bags", Category.GENERAL_WASTE, "Gray bin"),
            new BinRule("Batteries", Category.HAZARDOUS, "Hazardous drop-off"),
            new BinRule("Electronics", Category.HAZARDOUS, "WEEE drop-off")
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
