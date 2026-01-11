package com.example.nesvie_copyzalo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LichSuDangNhapActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private DBHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_dang_nhap);

        rvHistory = findViewById(R.id.rvHistory);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageView btnBack = findViewById(R.id.btnBack);

        dbHelper = new DBHelper(this);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Lấy userId từ session
        currentUserId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("current_user_id", null);

        if (currentUserId != null) {
            List<LoginHistory> historyList = dbHelper.getLoginHistory(currentUserId);

            // Nếu chưa có thì fake dữ liệu demo
            if (historyList.isEmpty()) {
                dbHelper.addLoginHistory(currentUserId, "Android Phone", System.currentTimeMillis());
                dbHelper.addLoginHistory(currentUserId, "Android Phone", System.currentTimeMillis() - 3600 * 1000);
                dbHelper.addLoginHistory(currentUserId, "Android Phone", System.currentTimeMillis() - 7200 * 1000);

                historyList = dbHelper.getLoginHistory(currentUserId);
            }

            if (historyList.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvHistory.setLayoutManager(new LinearLayoutManager(this));
                rvHistory.setAdapter(new LoginHistoryAdapter(historyList));
            }
        }
    }
}
