package com.example.recyclescan3.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryScanDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME    = "history.db";
    static final int    DATABASE_VERSION = 1;

    private static final String SQL_CREATE =
        "CREATE TABLE history (" +
        HistoryScanContract.COL_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        HistoryScanContract.COL_BARCODE      + " TEXT, " +
        HistoryScanContract.COL_PRODUCT_NAME + " TEXT NOT NULL, " +
        HistoryScanContract.COL_CATEGORY     + " TEXT NOT NULL, " +
        HistoryScanContract.COL_SCANNED_AT   + " INTEGER NOT NULL)";

    public HistoryScanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) { db.execSQL(SQL_CREATE); }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }
}
