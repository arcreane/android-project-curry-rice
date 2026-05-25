package com.example.recyclescan3.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryScanProvider extends ContentProvider {

    private static final int HISTORY    = 1;
    private static final int HISTORY_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(HistoryScanContract.AUTHORITY, "history",   HISTORY);
        URI_MATCHER.addURI(HistoryScanContract.AUTHORITY, "history/#", HISTORY_ID);
    }

    private HistoryDatabase dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new HistoryDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case HISTORY:
                cursor = db.query(HistoryDatabase.TABLE_HISTORY,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case HISTORY_ID:
                cursor = db.query(HistoryDatabase.TABLE_HISTORY,
                        projection,
                        HistoryScanContract.COL_ID + " = ?",
                        new String[]{ String.valueOf(ContentUris.parseId(uri)) },
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (URI_MATCHER.match(uri) != HISTORY) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }
        if (values != null && !values.containsKey(HistoryScanContract.COL_SCANNED_AT)) {
            values.put(HistoryScanContract.COL_SCANNED_AT, System.currentTimeMillis());
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(HistoryDatabase.TABLE_HISTORY, null, values);
        if (id == -1) return null;
        Uri newUri = ContentUris.withAppendedId(HistoryScanContract.CONTENT_URI, id);
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(newUri, null);
        }
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (URI_MATCHER.match(uri)) {
            case HISTORY:
                rowsDeleted = db.delete(HistoryDatabase.TABLE_HISTORY,
                        selection, selectionArgs);
                break;
            case HISTORY_ID:
                rowsDeleted = db.delete(HistoryDatabase.TABLE_HISTORY,
                        HistoryScanContract.COL_ID + " = ?",
                        new String[]{ String.valueOf(ContentUris.parseId(uri)) });
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsDeleted > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0; // Not required — stub satisfies the abstract method.
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case HISTORY:
                return "vnd.android.cursor.dir/vnd." + HistoryScanContract.AUTHORITY + ".history";
            case HISTORY_ID:
                return "vnd.android.cursor.item/vnd." + HistoryScanContract.AUTHORITY + ".history";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
