package com.example.recyclescan3.data;

import android.net.Uri;

public final class HistoryScanContract {
    private HistoryScanContract() {}

    public static final String AUTHORITY = "com.example.recyclescan3.provider";
    public static final Uri    CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/history");

    public static final String COL_ID           = "_id";
    public static final String COL_BARCODE      = "barcode";
    public static final String COL_PRODUCT_NAME = "product_name";
    public static final String COL_CATEGORY     = "category";
    public static final String COL_SCANNED_AT   = "scanned_at";
}
