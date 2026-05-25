package com.example.recyclescan3;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.example.recyclescan3.data.HistoryScanContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recyclescan3.data.HistoryScanContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ResultActivity — shown after a product has been identified.
 *
 * Receives a {@link Product} via Intent extra {@link #EXTRA_PRODUCT}.
 * Displays:
 *   • Product name and barcode
 *   • Bin category (label + emoji)
 *   • Optional sorting instructions
 *   • A "Scan again" button that finishes this Activity (returns to Scanner)
 *
 * R1 — navigated to from ScannerActivity via explicit Intent; triggers an
 *       implicit Intent (ACTION_SEND) when the user taps "Share" in the menu.
 * R2 — portrait and landscape layouts both defined; state survives rotation
 *       because all data comes from the Intent, not mutable UI state.
 * R4 — options menu with "Share" and "Add to History" actions.
 */
public class ResultActivity extends AppCompatActivity {

    /** Key used to pass a {@link Product} in the launching Intent. */
    public static final String EXTRA_PRODUCT = "extra_product";

    // ── Saved-state keys (R2 — survives rotation) ───────────────────────────
    // No extra saved state needed: the Product lives in the Intent, which
    // Android automatically preserves across configuration changes.

    // ── Fields ──────────────────────────────────────────────────────────────
    private Product product;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // ── Retrieve the product ────────────────────────────────────────────
        product = getIntent().getParcelableExtra(EXTRA_PRODUCT);

        if (product == null) {
            // Defensive: should never happen in normal flow.
            Toast.makeText(this, "No product data received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ── Back arrow in action bar ────────────────────────────────────────
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scan Result");
        }

        // ── Bind views ──────────────────────────────────────────────────────
        bindViews();
    }

    /** Populates all visible fields from the {@link #product}. */
    private void bindViews() {
        // Product name
        TextView tvName = findViewById(R.id.tv_product_name);
        tvName.setText(product.getName());

        // Barcode (shown only when available)
        TextView tvBarcode = findViewById(R.id.tv_barcode);
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            tvBarcode.setText(getString(R.string.label_barcode, product.getBarcode()));
        } else {
            tvBarcode.setText(R.string.label_barcode_unknown);
        }

        // Category card — emoji + label
        TextView tvCategoryEmoji = findViewById(R.id.tv_category_emoji);
        TextView tvCategoryLabel = findViewById(R.id.tv_category_label);
        tvCategoryEmoji.setText(product.getCategory().getEmoji());
        tvCategoryLabel.setText(product.getCategory().getLabel());

        // Apply the per-category style to the card so themes drive colours (R6).
        // The card view itself uses a style attribute; we switch the background
        // tint tag so the layout can reference it.
        // (Full colour wiring is done in themes.xml / styles.xml — see those files.)

        // Instructions (optional)
        TextView tvInstructions = findViewById(R.id.tv_instructions);
        if (product.getInstructions() != null && !product.getInstructions().isEmpty()) {
            tvInstructions.setText(product.getInstructions());
        } else {
            tvInstructions.setText(R.string.label_no_instructions);
        }

        // "Scan again" button — just pops this Activity off the back stack
        Button btnScanAgain = findViewById(R.id.btn_scan_again);
        btnScanAgain.setOnClickListener(v -> finish());
    }

    // ── Options menu (R4) ────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Back arrow
            finish();
            return true;

        } else if (id == R.id.action_share) {
            // R1 — implicit Intent: share the result as plain text
            shareResult();
            return true;

        } else if (id == R.id.action_add_history) {
            saveToHistory();
            addToHistory();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    /**
     * Fires an implicit {@link Intent#ACTION_SEND} so the user can share the
     * scan result via any app that handles plain text (R1 implicit Intent).
     */
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
}
