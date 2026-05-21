package com.example.recyclescan3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.recyclescan3.data.RegionRepository;
import com.example.recyclescan3.fragment.RegionRulesFragment;
import com.example.recyclescan3.model.Region;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    // Other activities that need the selected region can read from these same keys.
    public static final String PREFS_NAME = "recyclescan_prefs";
    public static final String KEY_REGION = "selected_region";

    private TextView tvCurrentRegion;
    private View settingsContent;
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvCurrentRegion = findViewById(R.id.tv_current_region);
        settingsContent = findViewById(R.id.settings_content);
        fragmentContainer = findViewById(R.id.fragment_container);

        Button btnViewRules = findViewById(R.id.btn_view_rules);
        btnViewRules.setOnClickListener(v -> openRulesFragment());

        refreshRegionLabel();
    }

    // Android calls this once to build the toolbar menu from our XML file.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_region, menu);
        return true;
    }

    // Called whenever a menu item (or the back arrow) is tapped.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_region) {
            showRegionPicker();
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            fragmentContainer.setVisibility(View.GONE);
            settingsContent.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void showRegionPicker() {
        List<Region> regions = RegionRepository.getAll();
        String[] names = new String[regions.size()];
        for (int i = 0; i < regions.size(); i++) {
            names[i] = regions.get(i).displayName;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select your region")
                .setItems(names, (dialog, which) -> {
                    saveRegion(regions.get(which).code);
                    refreshRegionLabel();
                })
                .show();
    }

    private void saveRegion(String code) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_REGION, code)
                .apply();
    }

    private void refreshRegionLabel() {
        String code = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_REGION, RegionRepository.getDefault().code);
        Region region = RegionRepository.getByCode(code);
        tvCurrentRegion.setText("Current region: " + region.displayName);
    }

    private void openRulesFragment() {
        String code = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_REGION, RegionRepository.getDefault().code);

        settingsContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, RegionRulesFragment.newInstance(code))
                .addToBackStack(null)
                .commit();
    }
}
