package com.example.recyclescan3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.recyclescan3.adapter.HistoryAdapter;
import com.example.recyclescan3.data.HistoryScanContract;
import com.example.recyclescan3.model.HistoryItem;
import com.example.recyclescan3.Product;
import com.example.recyclescan3.ResultActivity;
import com.example.recyclescan3.WasteCategory;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String KEY_FILTER = "filter";
    private static final String FILTER_ALL = "ALL";

    private String        currentFilter = FILTER_ALL;
    private RecyclerView  recyclerView;
    private View          emptyView;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_history));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            currentFilter = savedInstanceState.getString(KEY_FILTER, FILTER_ALL);
        }

        recyclerView = findViewById(R.id.rv_history);
        emptyView    = findViewById(R.id.layout_empty);

        adapter = new HistoryAdapter(this, item -> openResult(item));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FILTER, currentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_filter_all)        { currentFilter = FILTER_ALL;      loadHistory(); return true; }
        if (id == R.id.action_filter_recyclable) { currentFilter = "RECYCLABLE";    loadHistory(); return true; }
        if (id == R.id.action_filter_compost)    { currentFilter = "COMPOST";       loadHistory(); return true; }
        if (id == R.id.action_filter_general)    { currentFilter = "GENERAL_WASTE"; loadHistory(); return true; }
        if (id == R.id.action_filter_hazardous)  { currentFilter = "HAZARDOUS";     loadHistory(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void loadHistory() {
        new Thread(() -> {
            String selection = null;
            String[] selectionArgs = null;
            if (!currentFilter.equals(FILTER_ALL)) {
                selection     = HistoryScanContract.COL_CATEGORY + " = ?";
                selectionArgs = new String[]{ currentFilter };
            }

            Cursor cursor = getContentResolver().query(
                    HistoryScanContract.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    HistoryScanContract.COL_SCANNED_AT + " DESC"
            );

            List<HistoryItem> items = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    items.add(new HistoryItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(HistoryScanContract.COL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HistoryScanContract.COL_BARCODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HistoryScanContract.COL_PRODUCT_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HistoryScanContract.COL_CATEGORY)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(HistoryScanContract.COL_SCANNED_AT))
                    ));
                }
                cursor.close();
            }

            List<HistoryItem> finalItems = items;
            runOnUiThread(() -> {
                adapter.updateData(finalItems);
                recyclerView.setVisibility(finalItems.isEmpty() ? View.GONE    : View.VISIBLE);
                emptyView.setVisibility(   finalItems.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }


    private void openResult(HistoryItem item) {
        WasteCategory wc;
        try {
            wc = WasteCategory.valueOf(item.getCategoryName());
        } catch (IllegalArgumentException e) {
            wc = WasteCategory.GENERAL_WASTE;
        }
        Product product = new Product(
                item.getProductName(),
                item.getBarcode(),
                wc,
                null
        );
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_PRODUCT, product);
        intent.putExtra(ResultActivity.EXTRA_FROM_HISTORY, true);
        startActivity(intent);
    }
}
