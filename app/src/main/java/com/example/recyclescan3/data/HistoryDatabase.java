package com.example.recyclescan3.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME    = "recyclescan_history.db";
    private static final int    DB_VERSION = 1;

    public static final String TABLE_HISTORY = "history";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " ("
            + HistoryScanContract.COL_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + HistoryScanContract.COL_BARCODE      + " TEXT, "
            + HistoryScanContract.COL_PRODUCT_NAME + " TEXT, "
            + HistoryScanContract.COL_CATEGORY     + " TEXT NOT NULL, "
            + HistoryScanContract.COL_SCANNED_AT   + " INTEGER NOT NULL"
            + ")";

    private static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_HISTORY;

    public HistoryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}
