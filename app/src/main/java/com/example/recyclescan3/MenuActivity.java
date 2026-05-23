package com.example.recyclescan3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.recyclescan3.data.RegionRepository;
import com.example.recyclescan3.model.Region;

import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Spinner regionSpinner;
    private TextView tvRulesDisplay;
    private Button btnScanner, btnHistory, btnSettings;
    private List<Region> regions;
    private boolean isInitialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("RecycleScan - Select Region");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get views
        regionSpinner = findViewById(R.id.spinner_region);
        tvRulesDisplay = findViewById(R.id.tv_rules);
        btnScanner = findViewById(R.id.btn_go_scanner);
        btnHistory = findViewById(R.id.btn_go_history);
        btnSettings = findViewById(R.id.btn_go_settings);

        // Get regions from repository
        regions = RegionRepository.getAll();

        // Setup spinner
        setupSpinner();

        // Setup navigation buttons
        setupNavigationButtons();

        // Display rules for current region
        displayCurrentRulesFromSettings();
    }

    private void setupSpinner() {
        // Create array of region display names
        String[] regionNames = new String[regions.size()];
        for (int i = 0; i < regions.size(); i++) {
            regionNames[i] = regions.get(i).displayName;
        }

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                regionNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(adapter);

        // Get currently selected region from settings
        String currentRegionCode = getSharedPreferences(
                SettingsActivity.PREFS_NAME,
                MODE_PRIVATE
        ).getString(SettingsActivity.KEY_REGION, RegionRepository.getDefault().code);

        // Find and set the currently selected region in spinner
        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i).code.equals(currentRegionCode)) {
                regionSpinner.setSelection(i, false);
                break;
            }
        }

        // Handle spinner selection changes
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (isInitialLoad) {
                    isInitialLoad = false;
                    return;
                }

                Region selectedRegion = regions.get(position);

                // Save to SharedPreferences
                getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(SettingsActivity.KEY_REGION, selectedRegion.code)
                        .apply();

                // Update display
                displayRulesForRegion(selectedRegion);

                // Show toast confirmation
                Toast.makeText(MenuActivity.this,
                        "Region changed to: " + selectedRegion.displayName,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayCurrentRulesFromSettings() {
        String regionCode = getSharedPreferences(
                SettingsActivity.PREFS_NAME,
                MODE_PRIVATE
        ).getString(SettingsActivity.KEY_REGION, RegionRepository.getDefault().code);

        Region region = RegionRepository.getByCode(regionCode);
        displayRulesForRegion(region);
    }

    private void displayRulesForRegion(Region region) {
        StringBuilder rulesText = new StringBuilder();
        rulesText.append("-> ").append(region.displayName).append("\n\n");
        rulesText.append("Waste Sorting Rules:\n");
        rulesText.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        for (int i = 0; i < region.rules.size(); i++) {
            rulesText.append("• ").append(region.rules.get(i).productType).append("\n");
            rulesText.append("  → ").append(region.rules.get(i).binLabel).append("\n\n");
        }

        tvRulesDisplay.setText(rulesText.toString());
    }

    private void setupNavigationButtons() {
        btnScanner.setOnClickListener(v -> navigateToActivity("Scanner"));
        btnHistory.setOnClickListener(v -> navigateToActivity("History"));
        btnSettings.setOnClickListener(v -> navigateToActivity("Settings"));
    }

    private void navigateToActivity(String activityName) {
        Intent intent;
        switch (activityName) {
            case "Scanner":
                Toast.makeText(this, "Scanner Activity not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            case "History":

                Toast.makeText(this, "History Activity not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            case "Settings":
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}