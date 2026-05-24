# PLAN.md — RecycleScan3: TFLite + History Integration

## Overview

RecycleScan3 is an Android app (Java, minSdk 34) that lets users scan waste items with the camera.
A Google Teachable Machine model (`model_unquant.tflite`, 5 classes) classifies the captured image
into one of four waste categories. This plan covers:

1. Real ML inference replacing the hardcoded stub in `ScannerActivity`
2. A working `ContentProvider` for scan history (fixes the `HistoryActivity` crash)
3. Enum unification (`WasteCategory` vs `Category`)

### Label → Category mapping

| Label (labels.txt)            | WasteCategory   |
|-------------------------------|-----------------|
| `poubelle_jaune`              | RECYCLABLE      |
| `poubelle_verre`              | RECYCLABLE      |
| `ordures_menageres`           | GENERAL_WASTE   |
| `decheterie_collecte_speciale`| HAZARDOUS       |
| `compost_biodechets`          | COMPOST         |

---

## Recommended implementation order

1. Phase 1 — enum unification (unlocks clean compile)
2. Phase 2 + 3 — TFLite dependencies + copy model files to assets
3. Phase 4 — `TFLiteClassifier.java`
4. Phase 5 — wire classifier into `ScannerActivity`
5. Phase 6 — `ContentProvider` + SQLite + Manifest
6. Phase 7 — wire "Add to History" in `ResultActivity`

Build and run after each phase to catch issues early.

---

## Phase 1 — Enum Unification

**Goal:** Eliminate the `WasteCategory` / `Category` split. One enum everywhere.

**Decision:** Keep `WasteCategory` as the single source of truth. Rename `GENERAL` → `GENERAL_WASTE`
(matches DB filter strings in `HistoryActivity`). Delete `model/Category.java` after migrating callers.

### Files to change

| File | Change |
|------|--------|
| `WasteCategory.java` | Rename `GENERAL` → `GENERAL_WASTE` |
| `model/HistoryItem.java` | Change field type `Category` → `WasteCategory` |
| `model/BinRule.java` | Change field type `Category` → `WasteCategory` |
| `data/RegionRepository.java` | Replace `Category.XXX` → `WasteCategory.XXX`; fix import |
| `adapter/HistoryAdapter.java` | Replace `Category` → `WasteCategory`; fix `GENERAL_WASTE` in switch |
| `HistoryActivity.java` | Remove `Category` import; replace `Category.valueOf(...)` → `WasteCategory.valueOf(...)` |
| `fragment/RegionRulesFragment.java` | Update any `Category` references to `WasteCategory` |
| `model/Category.java` | **Delete** once all callers are migrated |

> **Note on ordinals:** `WasteCategory` is serialized via `ordinal()` in `Product.writeToParcel`.
> Renaming `GENERAL` → `GENERAL_WASTE` keeps constants in the same position, so no Parcel breakage.

---

## Phase 2 — TFLite Dependency Setup

**File: `gradle/libs.versions.toml`**

```toml
[versions]
# add:
tflite = "2.16.1"

[libraries]
# add:
tflite         = { group = "org.tensorflow", name = "tensorflow-lite",         version.ref = "tflite" }
tflite-support = { group = "org.tensorflow", name = "tensorflow-lite-support", version.ref = "tflite" }
```

**File: `app/build.gradle.kts`**

Inside `android { }`, add:
```kotlin
aaptOptions {
    noCompress += "tflite"
}
```

Inside `dependencies { }`, add:
```kotlin
implementation(libs.tflite)
implementation(libs.tflite.support)
```

> If `noCompress +=` causes a type error under AGP 9.x, use `noCompress("tflite")` instead.
> If `2.16.1` fails to resolve, try `2.15.0` or `2.14.0`.

---

## Phase 3 — Copy Model Files to Assets

Create the directory (does not yet exist):
```
app/src/main/assets/
```

Copy from `converted_tflite/` into `app/src/main/assets/`:
- `model_unquant.tflite`
- `labels.txt`

Keep them at the root of `assets/` — the classifier references them by name directly.

---

## Phase 4 — TFLiteClassifier Helper Class

**New file: `app/src/main/java/com/example/recyclescan3/ml/TFLiteClassifier.java`**

```java
package com.example.recyclescan3.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.recyclescan3.WasteCategory;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier implements Closeable {

    public static class Result {
        public final String label;
        public final float  confidence;
        public final WasteCategory category;

        Result(String label, float confidence, WasteCategory category) {
            this.label      = label;
            this.confidence = confidence;
            this.category   = category;
        }
    }

    private final Interpreter   interpreter;
    private final List<String>  labels;

    public TFLiteClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        labels      = loadLabels(context);
    }

    public Result classify(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        float[][][][] input  = new float[1][224][224][3];
        float[][]     output = new float[1][labels.size()];

        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int px = scaled.getPixel(x, y);
                input[0][y][x][0] = Color.red(px)   / 255f;
                input[0][y][x][1] = Color.green(px) / 255f;
                input[0][y][x][2] = Color.blue(px)  / 255f;
            }
        }

        interpreter.run(input, output);

        int   best  = 0;
        float score = output[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > score) { score = output[0][i]; best = i; }
        }

        String label = labels.get(best);
        return new Result(label, score, mapLabel(label));
    }

    @Override
    public void close() {
        interpreter.close();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MappedByteBuffer loadModelFile(Context ctx) throws IOException {
        android.content.res.AssetFileDescriptor fd =
                ctx.getAssets().openFd("model_unquant.tflite");
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel ch = fis.getChannel();
        return ch.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    private List<String> loadLabels(Context ctx) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(ctx.getAssets().open("labels.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length == 2) list.add(parts[1]);
        }
        br.close();
        return list;
    }

    private static WasteCategory mapLabel(String label) {
        switch (label) {
            case "poubelle_jaune":                return WasteCategory.RECYCLABLE;
            case "poubelle_verre":                return WasteCategory.RECYCLABLE;
            case "ordures_menageres":             return WasteCategory.GENERAL_WASTE;
            case "decheterie_collecte_speciale":  return WasteCategory.HAZARDOUS;
            case "compost_biodechets":            return WasteCategory.COMPOST;
            default:                              return WasteCategory.GENERAL_WASTE;
        }
    }
}
```

---

## Phase 5 — Wire Classifier into ScannerActivity

**File: `app/src/main/java/com/example/recyclescan3/ScannerActivity.java`**

### Changes

1. Add field:
```java
private TFLiteClassifier classifier;
```

2. In `onCreate()`, after camera permission block:
```java
try {
    classifier = new TFLiteClassifier(this);
} catch (IOException e) {
    Log.e(TAG, "Failed to load TFLite model", e);
    statusText.setText("Model load failed");
}
```

3. In `onImageSaved()`, replace the hardcoded stub:
```java
statusText.setText("Classifying...");
Bitmap raw = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
if (raw == null || classifier == null) {
    statusText.setText("Classification failed");
    return;
}
TFLiteClassifier.Result result = classifier.classify(raw);
String name         = labelToDisplayName(result.label);
String instructions = String.format(Locale.US, "Confidence: %.0f%%", result.confidence * 100);
Product product = new Product(name, null, result.category, instructions);
Intent intent = new Intent(ScannerActivity.this, ResultActivity.class);
intent.putExtra(ResultActivity.EXTRA_PRODUCT, product);
startActivity(intent);
```

4. Add helper:
```java
private String labelToDisplayName(String label) {
    switch (label) {
        case "poubelle_jaune":                return "Yellow Bin Item";
        case "poubelle_verre":                return "Glass Item";
        case "ordures_menageres":             return "Household Waste";
        case "decheterie_collecte_speciale":  return "Special Collection Item";
        case "compost_biodechets":            return "Compostable Item";
        default:                              return "Unknown Item";
    }
}
```

5. In `onDestroy()`:
```java
if (classifier != null) classifier.close();
```

> `classify()` runs synchronously on the main thread. At 224×224 this is ~50–100 ms on API 34+
> devices — acceptable for a course project. If ANR occurs in testing, wrap in a background thread
> the same way `HistoryActivity.loadHistory()` does.

---

## Phase 6 — ContentProvider Implementation

### 6a. Database Helper

**New file: `app/src/main/java/com/example/recyclescan3/data/HistoryScanDbHelper.java`**

```java
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
```

### 6b. ContentProvider

**New file: `app/src/main/java/com/example/recyclescan3/data/HistoryScanProvider.java`**

```java
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
```

### 6c. Register in AndroidManifest.xml

Inside `<application>`, add:
```xml
<provider
    android:name=".data.HistoryScanProvider"
    android:authorities="com.example.recyclescan3.provider"
    android:exported="false" />
```

---

## Phase 7 — Wire "Add to History" in ResultActivity

**File: `app/src/main/java/com/example/recyclescan3/ResultActivity.java`**

Replace the `action_add_history` Toast stub:
```java
} else if (id == R.id.action_add_history) {
    addToHistory();
    return true;
}
```

Add helper method:
```java
private void addToHistory() {
    ContentValues values = new ContentValues();
    values.put(HistoryScanContract.COL_PRODUCT_NAME, product.getName());
    values.put(HistoryScanContract.COL_BARCODE,      product.getBarcode());
    values.put(HistoryScanContract.COL_CATEGORY,     product.getCategory().name());
    values.put(HistoryScanContract.COL_SCANNED_AT,   System.currentTimeMillis());

    Uri inserted = getContentResolver().insert(HistoryScanContract.CONTENT_URI, values);
    int msgRes = inserted != null ? R.string.msg_added_to_history : R.string.msg_history_error;
    Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show();
}
```

Add imports: `android.content.ContentValues`, `android.net.Uri`, `com.example.recyclescan3.data.HistoryScanContract`.

Also add `R.string.msg_history_error` to `res/values/strings.xml`:
```xml
<string name="msg_history_error">Failed to save to history</string>
```

---

## Known Issues / Decisions

### Enum mismatch (`GENERAL` vs `GENERAL_WASTE`)
The rename in Phase 1 will trigger compile errors at every call site — that is intentional and good.
Fix them all before proceeding to Phase 2. The ordinal positions do not change so Parcel serialization is unaffected.

### Inference on main thread
`classify()` is synchronous and runs in `onImageSaved()` which already executes on the main executor.
~50–100 ms is fine for API 34 devices. If it causes ANR, move it off-thread.

### `aaptOptions` DSL in AGP 9.x
Use `noCompress += "tflite"` (Kotlin set addition). Fallback: `noCompress("tflite")`.

### TFLite version
`2.16.1` is current stable. Fallback: `2.15.0` or `2.14.0`.

### `assets/` directory
`app/src/main/assets/` does not yet exist — create it before copying model files.
