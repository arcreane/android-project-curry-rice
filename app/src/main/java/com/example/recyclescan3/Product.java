package com.curryrice.recyclscan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a scanned product.
 *
 * Implements Parcelable so an instance can be put directly into an Intent extra
 * and recovered in ResultActivity without serialisation overhead.
 *
 * Usage (Scanner side):
 *   Product p = new Product("Plastic bottle", "5000112637922", WasteCategory.RECYCLABLE, "Rinse before recycling.");
 *   Intent intent = new Intent(this, ResultActivity.class);
 *   intent.putExtra(ResultActivity.EXTRA_PRODUCT, p);
 *   startActivity(intent);
 *
 * Usage (Result side):
 *   Product p = getIntent().getParcelableExtra(ResultActivity.EXTRA_PRODUCT);
 */
public class Product implements Parcelable {

    // ── Fields ──────────────────────────────────────────────────────────────

    /** Human-readable product name (from barcode lookup or manual entry). */
    private final String name;

    /** Raw barcode / QR string as scanned. May be null if photo-only identification. */
    private final String barcode;

    /** Sorting bin assigned to this product. */
    private final WasteCategory category;

    /** Optional extra instructions (e.g. "Remove cap before recycling"). May be null. */
    private final String instructions;

    // ── Constructor ─────────────────────────────────────────────────────────

    public Product(String name, String barcode, WasteCategory category, String instructions) {
        this.name         = name;
        this.barcode      = barcode;
        this.category     = category;
        this.instructions = instructions;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public String       getName()         { return name; }
    public String       getBarcode()      { return barcode; }
    public WasteCategory getCategory()    { return category; }
    public String       getInstructions() { return instructions; }

    // ── Parcelable implementation ────────────────────────────────────────────

    protected Product(Parcel in) {
        name         = in.readString();
        barcode      = in.readString();
        category     = WasteCategory.values()[in.readInt()];
        instructions = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(barcode);
        dest.writeInt(category.ordinal());
        dest.writeString(instructions);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) { return new Product(in); }

        @Override
        public Product[] newArray(int size) { return new Product[size]; }
    };
}
