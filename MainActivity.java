package com.example.qqq3xstrategy.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.qqq3xstrategy.R;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.viewmodels.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main activity for the QQQ3X Strategy app
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 123;
    
    private MainViewModel viewModel;
    private TextView tvCurrentPosition;
    private TextView tvPositionSince;
    private TextView tvDaysInPosition;
    private TextView tvQqqPrice;
    private TextView tvVixValue;
    private TextView tvQqqSmaShort;
    private TextView tvQqqSmaLong;
    private TextView tvQqqSmaYear;
    private TextView tvQqqTrend;
    private TextView tvVixTrend;
    private TextView tvSignalStrength;
    private TextView tvLastUpdated;
    private Button btnRefreshData;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.d(TAG, "Notification permission denied");
                showPermissionDeniedDialog();
            }
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        tvCurrentPosition = findViewById(R.id.tv_current_position);
        tvPositionSince = findViewById(R.id.tv_position_since);
        tvDaysInPosition = findViewById(R.id.tv_days_in_position);
        tvQqqPrice = findViewById(R.id.tv_qqq_price);
        tvVixValue = findViewById(R.id.tv_vix_value);
        tvQqqSmaShort = findViewById(R.id.tv_qqq_sma_short);
        tvQqqSmaLong = findViewById(R.id.tv_qqq_sma_long);
        tvQqqSmaYear = findViewById(R.id.tv_qqq_sma_year);
        tvQqqTrend = findViewById(R.id.tv_qqq_trend);
        tvVixTrend = findViewById(R.id.tv_vix_trend);
        tvSignalStrength = findViewById(R.id.tv_signal_strength);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        btnRefreshData = findViewById(R.id.btn_refresh_data);
        
        // Set up view model
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // Set up observers
        setupObservers();
        
        // Set up click listeners
        setupClickListeners();
        
        // Check notification permission
        checkNotificationPermission();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_history) {
            startActivity(new Intent(this, StrategyHistoryActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setupObservers() {
        // Observe latest market data
        viewModel.getLatestMarketData().observe(this, this::updateMarketData);
        
        // Observe latest signal
        viewModel.getLatestSignal().observe(this, this::updateSignal);
    }
    
    private void setupClickListeners() {
        btnRefreshData.setOnClickListener(v -> {
            viewModel.refreshData();
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateMarketData(MarketData data) {
        if (data == null) return;
        
        tvQqqPrice.setText(String.format(Locale.US, "$%.2f", data.getQqqClose()));
        tvVixValue.setText(String.format(Locale.US, "%.2f", data.getVixClose()));
        
        // Update last updated time
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        tvLastUpdated.setText(sdf.format(new Date()));
    }
    
    private void updateSignal(SignalHistory signal) {
        if (signal == null) return;
        
        // Update current position
        boolean isLeveraged = signal.getSignal() == 1;
        tvCurrentPosition.setText(isLeveraged ? "LEVERAGED QQQ (3X)" : "SAFE ASSET");
        
        // Update position since date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        tvPositionSince.setText(sdf.format(signal.getDate()));
        
        // Update days in position
        long daysInPosition = (System.currentTimeMillis() - signal.getDate().toEpochDay() * 86400000L) / (24 * 60 * 60 * 1000);
        tvDaysInPosition.setText(String.valueOf(daysInPosition));
        
        // Update trend indicators
        tvQqqTrend.setText(isLeveraged ? "↗️ UPTREND" : "↘️ DOWNTREND");
        tvVixTrend.setText(isLeveraged ? "↘️ DOWNTREND" : "↗️ UPTREND");
        tvSignalStrength.setText(signal.getPositionChanged() ? "STRONG" : "STABLE");
    }
    
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                
                // Show rationale if needed
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationRationaleDialog();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }
    
    private void showNotificationRationaleDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Notification Permission")
            .setMessage("The QQQ3X Strategy app needs to send notifications to alert you when position changes are recommended. Without this permission, you may miss important trading signals.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            })
            .setNegativeButton("Not Now", null)
            .show();
    }
    
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Notification Permission")
            .setMessage("Notifications are required for timely strategy alerts. Please enable them in app settings.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Not Now", null)
            .show();
    }
}