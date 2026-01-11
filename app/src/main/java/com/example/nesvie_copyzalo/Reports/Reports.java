package com.example.nesvie_copyzalo.Reports;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nesvie_copyzalo.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Reports extends AppCompatActivity {

    private Spinner spinnerTimeMode;
    private LineChart chartTotalUsers, chartNewUsers, chartInactiveUsers, chartReturningUsers;
    private ImageView backArrow;
    private TextView tvTotalUsers, tvNewUsers, tvInactiveUsers, tvReturningUsers;

    private String selectedTimeMode = "Tuần";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initViews();
        setupEventListeners();
        loadAllReports();
    }

    private void initViews() {
        spinnerTimeMode = findViewById(R.id.spinnerTimeMode);
        chartTotalUsers = findViewById(R.id.chartTotalUsers);
        chartNewUsers = findViewById(R.id.chartNewUsers);
        chartInactiveUsers = findViewById(R.id.chartInactiveUsers);
        chartReturningUsers = findViewById(R.id.chartReturningUsers);
        backArrow = findViewById(R.id.backArrow);

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvNewUsers = findViewById(R.id.tvNewUsers);
        tvInactiveUsers = findViewById(R.id.tvInactiveUsers);
        tvReturningUsers = findViewById(R.id.tvReturningUsers);
    }

    private void setupEventListeners() {
        backArrow.setOnClickListener(v -> finish());

        spinnerTimeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeMode = spinnerTimeMode.getSelectedItem().toString();
                loadAllReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAllReports() {
        // Prepare time labels
        ArrayList<String> labels = prepareTimeLabels();

        // Generate sample data
        Map<String, Integer> totalUsersData = generateSampleData(labels, 50, 150);
        Map<String, Integer> newUsersData = generateSampleData(labels, 10, 50);
        Map<String, Integer> inactiveUsersData = generateSampleData(labels, 5, 30);
        Map<String, Integer> returningUsersData = generateSampleData(labels, 15, 60);

        // Update summary numbers
        updateSummaryNumbers(totalUsersData, newUsersData, inactiveUsersData, returningUsersData);

        // Setup charts
        setupLineChart(chartTotalUsers, totalUsersData, labels, "#3498db", "Tổng người dùng");
        setupLineChart(chartNewUsers, newUsersData, labels, "#27ae60", "Người đăng ký mới");
        setupLineChart(chartInactiveUsers, inactiveUsersData, labels, "#e74c3c", "Không hoạt động");
        setupLineChart(chartReturningUsers, returningUsersData, labels, "#f39c12", "Quay trở lại");
    }

    private ArrayList<String> prepareTimeLabels() {
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        Calendar today = Calendar.getInstance();

        switch (selectedTimeMode) {
            case "Tuần": {
                // 7 ngày trong tuần
                Calendar cal = (Calendar) today.clone();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                String[] daysOfWeek = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (int i = 0; i < 7; i++) {
                    String dateStr = sdfDay.format(cal.getTime());
                    labels.add(daysOfWeek[i] + "\n" + dateStr);
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
                break;
            }
            case "Tháng": {
                // 4 tuần trong tháng
                Calendar cal = (Calendar) today.clone();
                cal.add(Calendar.WEEK_OF_YEAR, -3);

                for (int i = 1; i <= 4; i++) {
                    String label = "Tuần " + i;
                    labels.add(label);
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                }
                break;
            }
            case "Năm":
            default: {
                // 12 tháng trong năm
                String[] months = {"T1", "T2", "T3", "T4", "T5", "T6",
                        "T7", "T8", "T9", "T10", "T11", "T12"};
                for (String month : months) {
                    labels.add(month);
                }
                break;
            }
        }
        return labels;
    }

    private Map<String, Integer> generateSampleData(ArrayList<String> labels, int min, int max) {
        Map<String, Integer> dataMap = new HashMap<>();
        Random random = new Random();

        int previousValue = min + random.nextInt((max - min) / 2);

        for (String label : labels) {
            // Tạo dữ liệu có xu hướng tăng dần với biến động nhẹ
            int change = random.nextInt(20) - 5; // -5 đến +15
            int value = Math.max(min, Math.min(max, previousValue + change));
            dataMap.put(label, value);
            previousValue = value;
        }

        return dataMap;
    }

    private void updateSummaryNumbers(Map<String, Integer> totalUsers, Map<String, Integer> newUsers,
                                      Map<String, Integer> inactiveUsers, Map<String, Integer> returningUsers) {
        int totalSum = 0;
        int newSum = 0;
        int inactiveSum = 0;
        int returningSum = 0;

        for (Integer value : totalUsers.values()) totalSum += value;
        for (Integer value : newUsers.values()) newSum += value;
        for (Integer value : inactiveUsers.values()) inactiveSum += value;
        for (Integer value : returningUsers.values()) returningSum += value;

        tvTotalUsers.setText(String.valueOf(totalSum));
        tvNewUsers.setText(String.valueOf(newSum));
        tvInactiveUsers.setText(String.valueOf(inactiveSum));
        tvReturningUsers.setText(String.valueOf(returningSum));
    }

    private void setupLineChart(LineChart chart, Map<String, Integer> dataMap, ArrayList<String> labels,
                                String colorHex, String label) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            entries.add(new Entry(i, dataMap.getOrDefault(labels.get(i), 0)));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);

        // Color settings
        int color = Color.parseColor(colorHex);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(color);

        // Fill gradient
        dataSet.setDrawFilled(true);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor(colorHex + "60"), Color.TRANSPARENT}
        );
        dataSet.setFillDrawable(gradientDrawable);

        // Smooth curves
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // X-Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(selectedTimeMode.equals("Tuần") ? 0f : -45f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(9f);

        // Y-Axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.parseColor("#666666"));
        leftAxis.setTextSize(9f);

        chart.getAxisRight().setEnabled(false);

        // Chart appearance
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawBorders(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setExtraBottomOffset(10f);

        // Legend
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // Animation
        chart.animateXY(1000, 1000);
        chart.invalidate();
    }
}