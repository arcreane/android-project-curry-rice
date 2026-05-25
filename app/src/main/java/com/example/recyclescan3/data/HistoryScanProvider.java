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

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(HistoryScanContract.AUTHORITY, "history",   HISTORY);
        uriMatcher.addURI(HistoryScanContract.AUTHORITY, "history/#", HISTORY_ID);
    }

    private HistoryScanDbHelper dbHelper;

    @Override public boolean onCreate() {
        dbHelper = new HistoryScanDbHelper(getContext());
        return true;
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (uriMatcher.match(uri) == HISTORY_ID) {
            cursor = db.query("history", projection,
                    HistoryScanContract.COL_ID + "=?",
                    new String[]{ String.valueOf(ContentUris.parseId(uri)) },
                    null, null, sortOrder);
        } else {
            cursor = db.query("history", projection, selection, selectionArgs, null, null, sortOrder);
        }
        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert("history", null, values);
        if (id > 0 && getContext() != null) {
            Uri newUri = ContentUris.withAppendedId(HistoryScanContract.CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        if (uriMatcher.match(uri) == HISTORY_ID) {
            count = db.delete("history",
                    HistoryScanContract.COL_ID + "=?",
                    new String[]{ String.valueOf(ContentUris.parseId(uri)) });
        } else {
            count = db.delete("history", selection, selectionArgs);
        }
        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override public int update(@NonNull Uri uri, ContentValues v, String s, String[] a) { return 0; }

    @Nullable @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.example.recyclescan3.history";
    }
}
