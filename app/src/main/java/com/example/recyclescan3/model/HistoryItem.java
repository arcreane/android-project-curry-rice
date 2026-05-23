package com.example.recyclescan3.model;

public class HistoryItem {
    private final long     id;
    private final String   barcode;
    private final String   productName;
    private final Category category;
    private final long     scannedAt;

    public HistoryItem(long id, String barcode, String productName,
                       Category category, long scannedAt) {
        this.id          = id;
        this.barcode     = barcode;
        this.productName = productName;
        this.category    = category;
        this.scannedAt   = scannedAt;
    }

    public long     getId()          { return id; }
    public String   getBarcode()     { return barcode; }
    public String   getProductName() { return productName; }
    public Category getCategory()    { return category; }
    public long     getScannedAt()   { return scannedAt; }
}
