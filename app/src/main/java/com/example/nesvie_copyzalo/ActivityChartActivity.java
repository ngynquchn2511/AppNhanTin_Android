package com.example.nesvie_copyzalo;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ActivityChartActivity extends AppCompatActivity {

    private BarChart barChart;
    private Button btnDay, btnWeek, btnYear;
    private TextView tvTotalTime, tvAverage, tvMaxTime;
    private DBHelper dbHelper;
    private String currentUserId;
    private String currentPeriod = "day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getStringExtra("userId");
        if (currentUserId == null) {
            currentUserId = "1";
        }

        barChart = findViewById(R.id.barChart);
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnYear = findViewById(R.id.btnYear);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvAverage = findViewById(R.id.tvAverage);
        tvMaxTime = findViewById(R.id.tvMaxTime);
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        setupChart();
        fakeDatabaseData();
        loadDayData();

        btnDay.setOnClickListener(v -> {
            currentPeriod = "day";
            updateButtonStyles();
            loadDayData();
        });

        btnWeek.setOnClickListener(v -> {
            currentPeriod = "week";
            updateButtonStyles();
            loadWeekData();
        });

        btnYear.setOnClickListener(v -> {
            currentPeriod = "year";
            updateButtonStyles();
            loadYearData();
        });
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));

        barChart.getAxisRight().setEnabled(false);
    }

    private void updateButtonStyles() {
        btnDay.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnWeek.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnYear.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));

        if (currentPeriod.equals("day")) {
            btnDay.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        } else if (currentPeriod.equals("week")) {
            btnWeek.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        } else {
            btnYear.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        }
    }

    private void loadDayData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            long startTime = cal.getTimeInMillis();

            float duration = getDayActivity(startTime);
            entries.add(new BarEntry(6 - i, duration / 60f)); // phút -> giờ
            labels.add(sdf.format(cal.getTime()));
        }

        updateChart(entries, labels, "Thời gian (giờ)");
        updateStats(entries);
    }

    private void loadWeekData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 3; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.WEEK_OF_YEAR, -i);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            long startTime = cal.getTimeInMillis();

            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.WEEK_OF_YEAR, 1);

            float duration = getWeekActivity(startTime, endCal.getTimeInMillis());
            entries.add(new BarEntry(3 - i, duration / 60f));

            String label = sdf.format(cal.getTime()) + "-" + sdf.format(endCal.getTime());
            labels.add(label);
        }

        updateChart(entries, labels, "Thời gian (giờ)");
        updateStats(entries);
    }

    private void loadYearData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        String[] months = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
        Calendar cal = Calendar.getInstance();

        for (int i = 11; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.MONTH, -i);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            long startTime = cal.getTimeInMillis();

            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.MONTH, 1);

            float duration = getMonthActivity(startTime, endCal.getTimeInMillis());
            entries.add(new BarEntry(11 - i, duration / 60f));

            labels.add(months[cal.get(Calendar.MONTH)]);
        }

        updateChart(entries, labels, "Thời gian (giờ)");
        updateStats(entries);
    }

    private void updateChart(ArrayList<BarEntry> entries, ArrayList<String> labels, String yAxisLabel) {
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#667eea"), Color.parseColor("#764ba2"),
                Color.parseColor("#f093fb"), Color.parseColor("#f5576c"));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.invalidate();
    }

    private void updateStats(ArrayList<BarEntry> entries) {
        float total = 0;
        float max = 0;

        for (BarEntry entry : entries) {
            total += entry.getY();
            if (entry.getY() > max) {
                max = entry.getY();
            }
        }

        float average = entries.size() > 0 ? total / entries.size() : 0;

        tvTotalTime.setText(String.format(Locale.getDefault(), "%.1fh", total));
        tvAverage.setText(String.format(Locale.getDefault(), "%.1fh", average));
        tvMaxTime.setText(String.format(Locale.getDefault(), "%.1fh", max));
    }

    private float getDayActivity(long timestamp) {
        List<ActivityRecord> records = dbHelper.getActivityData(currentUserId, "Ngày");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String targetDate = sdf.format(cal.getTime());

        for (ActivityRecord record : records) {
            if (record.getLabel().equals(targetDate)) {
                return record.getDuration(); // float
            }
        }
        return 0f;
    }

    private float getWeekActivity(long startTime, long endTime) {
        float total = 0f;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);

        while (cal.getTimeInMillis() < endTime) {
            total += getDayActivity(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return total;
    }

    private float getMonthActivity(long startTime, long endTime) {
        float total = 0f;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);

        while (cal.getTimeInMillis() < endTime) {
            total += getDayActivity(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return total;
    }

    private void fakeDatabaseData() {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua (chạy trực tiếp trên main thread, tránh lỗi connection pool)
        int existing = dbHelper.getActivityCount(currentUserId);
        if (existing > 0) {
            android.util.Log.d("FAKE_DATA", "🔹 Dữ liệu đã có sẵn (" + existing + " bản ghi). Bỏ qua fake.");
            return;
        }

        Calendar cal = Calendar.getInstance();

        for (int i = 364; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            long timestamp = cal.getTimeInMillis();

            // Random thời lượng hoạt động (phút)
            int duration;
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                // Cuối tuần ít hoạt động hơn
                duration = 30 + (int) (Math.random() * 60);  // 30–90 phút
            } else {
                // Ngày thường hoạt động nhiều hơn
                duration = 60 + (int) (Math.random() * 180); // 60–240 phút
            }

            // Lưu vào database
            dbHelper.addUserActivity(currentUserId, timestamp, duration);
        }

        // Log kết quả
        int count = dbHelper.getActivityCount(currentUserId);
        android.util.Log.d("FAKE_DATA", " Fake dữ liệu xong (" + count + " bản ghi) cho userId=" + currentUserId);

        // Cập nhật giao diện
        if (currentPeriod.equals("day")) {
            loadDayData();
        } else if (currentPeriod.equals("week")) {
            loadWeekData();
        } else {
            loadYearData();
        }
    }

}
