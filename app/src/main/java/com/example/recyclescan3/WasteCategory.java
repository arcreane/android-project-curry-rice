package com.example.recyclescan3;

/**
 * The four bin categories supported by RecycleScan.
 * Each category carries a display label and the color attribute name
 * used in themes (resolved at runtime via the style system — no hard-coded colors here).
 */
public enum WasteCategory {

    RECYCLABLE   ("Recyclable",      "♻️"),
    COMPOST      ("Compost",         "🌱"),
    GENERAL_WASTE("General Waste",   "🗑️"),
    HAZARDOUS    ("Hazardous Waste", "⚠️");

    private final String label;
    private final String emoji;

    WasteCategory(String label, String emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public String getLabel()  { return label; }
    public String getEmoji()  { return emoji; }
}
