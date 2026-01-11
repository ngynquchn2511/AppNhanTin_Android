package com.example.nesvie_copyzalo;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserActivityChartActivity extends AppCompatActivity {

    private BarChart barChart;
    private RadioGroup rgPeriod;
    private RadioButton rbDay, rbWeek, rbMonth;

    private DBHelper dbHelper;
    private String userId = "1"; // giả lập user test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activity_chart);

        barChart = findViewById(R.id.barChart);
        rgPeriod = findViewById(R.id.rgPeriod);
        rbDay = findViewById(R.id.rbDay);
        rbWeek = findViewById(R.id.rbWeek);
        rbMonth = findViewById(R.id.rbMonth);

        dbHelper = new DBHelper(this);

        // Thêm dữ liệu giả để test
        addFakeActivityData();

        // Vẽ chart mặc định theo "Ngày"
        drawChart("ngay");

        rgPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDay) {
                drawChart("ngay");
            } else if (checkedId == R.id.rbWeek) {
                drawChart("tuan");
            } else if (checkedId == R.id.rbMonth) {
                drawChart("thang");
            }
        });
    }

    private void drawChart(String period) {
        List<Integer> data = dbHelper.getActivitySummary(userId, period);
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Phút hoạt động");
        dataSet.setColor(getResources().getColor(R.color.red, null));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false); // ẩn mô tả
        barChart.getLegend().setEnabled(true);       // bật legend

        // XAxis hiển thị dưới
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.invalidate(); // refresh chart
    }

    // Hàm tạo dữ liệu giả
    private void addFakeActivityData() {
        Random rand = new Random();
        long now = System.currentTimeMillis();

        for (int i = 0; i < 30; i++) { // 30 ngày gần nhất
            long timestamp = now - i * 24 * 60 * 60 * 1000L; // trừ i ngày
            int duration = rand.nextInt(120); // 0-120 phút
            dbHelper.addUserActivity(userId, timestamp, duration);
        }
    }
}
