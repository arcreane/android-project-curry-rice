package com.example.recyclescan3;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recyclescan3.data.HistoryScanContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        product = getIntent().getParcelableExtra(EXTRA_PRODUCT);

        if (product == null) {
            Toast.makeText(this, "No product data received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scan Result");
        }

        bindViews();

        boolean fromHistory = getIntent().getBooleanExtra(EXTRA_FROM_HISTORY, false);
        if (!fromHistory) {
            saveToHistory();
        }
    }

    private void bindViews() {
        TextView tvName = findViewById(R.id.tv_product_name);
        tvName.setText(product.getName());

        TextView tvBarcode = findViewById(R.id.tv_barcode);
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            tvBarcode.setText(getString(R.string.label_barcode, product.getBarcode()));
        } else {
            tvBarcode.setText(R.string.label_barcode_unknown);
        }

        TextView tvCategoryEmoji = findViewById(R.id.tv_category_emoji);
        TextView tvCategoryLabel = findViewById(R.id.tv_category_label);
        tvCategoryEmoji.setText(product.getCategory().getEmoji());
        tvCategoryLabel.setText(product.getCategory().getLabel());

        TextView tvInstructions = findViewById(R.id.tv_instructions);
        if (product.getInstructions() != null && !product.getInstructions().isEmpty()) {
            tvInstructions.setText(product.getInstructions());
        } else {
            tvInstructions.setText(R.string.label_no_instructions);
        }

        Button btnScanAgain = findViewById(R.id.btn_scan_again);

        boolean fromHistory = getIntent().getBooleanExtra(EXTRA_FROM_HISTORY, false);
        btnScanAgain.setText(fromHistory ? "Back to History" : "Scan Again");

        btnScanAgain.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            shareResult();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void saveToHistory() {
        ContentValues values = new ContentValues();
        values.put(HistoryScanContract.COL_PRODUCT_NAME, product.getName());
        values.put(HistoryScanContract.COL_BARCODE,      product.getBarcode());
        values.put(HistoryScanContract.COL_CATEGORY,     product.getCategory().name());
        // COL_SCANNED_AT is auto-stamped by the provider when absent.

        Uri result = getContentResolver().insert(HistoryScanContract.CONTENT_URI, values);
        if (result != null) {
            Toast.makeText(this, R.string.msg_added_to_history, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.msg_history_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareResult() {
        String shareText = getString(
                R.string.share_template,
                product.getName(),
                product.getCategory().getLabel(),
                product.getCategory().getEmoji()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
    }

    public static final String EXTRA_FROM_HISTORY = "extra_from_history";

}
