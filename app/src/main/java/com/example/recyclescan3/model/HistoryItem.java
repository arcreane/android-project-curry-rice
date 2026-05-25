package com.example.recyclescan3.model;

public class HistoryItem {
    private final long     id;
    private final String   barcode;
    private final String   productName;
    private final String   categoryName;
    private final long     scannedAt;

    public HistoryItem(long id, String barcode, String productName,
                       String categoryName, long scannedAt) {
        this.id           = id;
        this.barcode      = barcode;
        this.productName  = productName;
        this.categoryName = categoryName;
        this.scannedAt    = scannedAt;
    }

    public long   getId()            { return id; }
    public String getBarcode()       { return barcode; }
    public String getProductName()   { return productName; }
    public String getCategoryName()  { return categoryName; }
    public long   getScannedAt()     { return scannedAt; }
}
