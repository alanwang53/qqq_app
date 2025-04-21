package com.example.qqq3xstrategy.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.qqq3xstrategy.R;
import com.example.qqq3xstrategy.viewmodels.PositionChangeViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying position change details
 */
public class PositionChangeDetailsActivity extends AppCompatActivity {
    private static final String TAG = "PositionChangeActivity";
    private static final int POSITION_CHANGE_NOTIFICATION_ID = 1001;
    
    private PositionChangeViewModel viewModel;
    private TextView tvRecommendation;
    private TextView tvPreviousPosition;
    private TextView tvHeldFor;
    private TextView tvVixOpen;
    private TextView tvQqqOpen;
    private TextView tvQqqYearSma;
    private TextView tvQqqShortSma;
    private TextView tvQqqLongSma;
    private TextView tvVixLt21;
    private TextView tvVixGt66;
    private TextView tvSignalConfidence;
    private Button btnMarkActioned;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_change_details);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Position Change");
        }
        
        // Initialize views
        tvRecommendation = findViewById(R.id.tv_recommendation);
        tvPreviousPosition = findViewById(R.id.tv_previous_position);
        tvHeldFor = findViewById(R.id.tv_held_for);
        tvVixOpen = findViewById(R.id.tv_vix_open);
        tvQqqOpen = findViewById(R.id.tv_qqq_open);
        tvQqqYearSma = findViewById(R.id.tv_qqq_year_sma);
        tvQqqShortSma = findViewById(R.id.tv_qqq_short_sma);
        tvQqqLongSma = findViewById(R.id.tv_qqq_long_sma);
        tvVixLt21 = findViewById(R.id.tv_vix_lt_21);
        tvVixGt66 = findViewById(R.id.tv_vix_gt_66);
        tvSignalConfidence = findViewById(R.id.tv_signal_confidence);
        btnMarkActioned = findViewById(R.id.btn_mark_actioned);
        
        // Get signal from intent
        int signal = getIntent().getIntExtra("signal", 0);
        
        // Set up view model
        viewModel = new ViewModelProvider(this).get(PositionChangeViewModel.class);
        
        // Set up observers
        setupObservers();
        
        // Set up click listeners
        setupClickListeners();
        
        // Mark notification as read
        NotificationManagerCompat.from(this).cancel(POSITION_CHANGE_NOTIFICATION_ID);
        
        // Update UI with signal
        updateRecommendation(signal);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupObservers() {
        // Observe latest market data
        viewModel.getLatestMarketData().observe(this, data -> {
            if (data == null) return;
            
            tvVixOpen.setText(String.format(Locale.US, "%.2f (%.1f%% from close)", 
                    data.getVixOpen(), 
                    (data.getVixOpen() / data.getVixClose() - 1) * 100));
            
            tvQqqOpen.setText(String.format(Locale.US, "$%.2f (%.1f%% from close)", 
                    data.getQqqOpen(), 
                    (data.getQqqOpen() / data.getQqqClose() - 1) * 100));
        });
        
        // Observe technical indicators
        viewModel.getLatestIndicators().observe(this, indicators -> {
            if (indicators == null) return;
            
            tvQqqYearSma.setText(String.format(Locale.US, "$%.2f", indicators.getQqqSmaYear()));
            tvQqqShortSma.setText(String.format(Locale.US, "$%.2f", indicators.getQqqSmaShort()));
            tvQqqLongSma.setText(String.format(Locale.US, "$%.2f", indicators.getQqqSmaLong()));
        });
        
        // Observe previous signal
        viewModel.getPreviousSignal().observe(this, signal -> {
            if (signal == null) return;
            
            tvPreviousPosition.setText(signal.getSignal() == 1 ? "LEVERAGED QQQ (3X)" : "SAFE ASSET (" + signal.getSafeAsset() + ")");
            
            // Calculate days held
            long daysHeld = (System.currentTimeMillis() - signal.getDate().toEpochDay() * 86400000L) / (24 * 60 * 60 * 1000);
            tvHeldFor.setText(daysHeld + " days");
        });
    }
    
    private void setupClickListeners() {
        btnMarkActioned.setOnClickListener(v -> {
            // Mark as actioned in preferences
            viewModel.markAsActioned();
            
            // Show confirmation and finish
            btnMarkActioned.setText("Marked as Actioned");
            btnMarkActioned.setEnabled(false);
            
            // Finish after delay
            btnMarkActioned.postDelayed(this::finish, 1500);
        });
    }
    
    private void updateRecommendation(int signal) {
        boolean isLeveraged = signal == 1;
        
        // Update recommendation
        tvRecommendation.setText(isLeveraged ? "SWITCH TO: LEVERAGED QQQ (3X)" : "SWITCH TO: SAFE ASSET");
        
        // Update condition indicators
        tvVixLt21.setText(isLeveraged ? "YES" : "NO");
        tvVixGt66.setText(isLeveraged ? "NO" : "YES");
        
        // Update signal confidence
        tvSignalConfidence.setText("HIGH");
    }
}