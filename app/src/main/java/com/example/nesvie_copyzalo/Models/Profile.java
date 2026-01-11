package com.example.nesvie_copyzalo.Models;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nesvie_copyzalo.DBHelper;
import com.example.nesvie_copyzalo.R;
import com.example.nesvie_copyzalo.User;

public class Profile extends AppCompatActivity {

    public static final int RESULT_PROFILE_UPDATED = 101; // hằng số gửi về Fragment

    private EditText edtHoTen;
    private RadioButton rdNam, rdNu, rdKhac;
    private Button btnLuu;
    private ImageView backArrow;

    private DBHelper dbHelper;
    private String currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ view
        edtHoTen = findViewById(R.id.edtHoTen);
        rdNam = findViewById(R.id.RdNam);
        rdNu = findViewById(R.id.RdNu);
        rdKhac = findViewById(R.id.RdKhac);
        btnLuu = findViewById(R.id.btnLuu);
        backArrow = findViewById(R.id.backArrow);

        dbHelper = new DBHelper(this);

        // Lấy currentUserId từ Intent
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("user_id");
        if (TextUtils.isEmpty(currentUserId)) {
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            currentUserId = prefs.getString("current_user_id", null);
        }

        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = dbHelper.getUserById(currentUserId);
        if (currentUser != null) {
            if (!TextUtils.isEmpty(currentUser.getTenHienThi())) {
                edtHoTen.setText(currentUser.getTenHienThi());
            }
            if ("Nam".equals(currentUser.getGioiTinh())) rdNam.setChecked(true);
            else if ("Nữ".equals(currentUser.getGioiTinh())) rdNu.setChecked(true);
            else rdKhac.setChecked(true);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Nút Lưu
        btnLuu.setOnClickListener(v -> {
            String tenMoi = edtHoTen.getText().toString().trim();
            String gioiTinhMoi = rdNam.isChecked() ? "Nam" : rdNu.isChecked() ? "Nữ" : "Khác";

            if (TextUtils.isEmpty(tenMoi)) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setTenHienThi(tenMoi);
            currentUser.setGioiTinh(gioiTinhMoi);

            int rowsAffected = dbHelper.updateUser(currentUser);
            if (rowsAffected > 0) {
                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                prefs.edit().putString("current_user_name", tenMoi).apply();

                Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show();

                // Gửi kết quả về Fragment_canhan
                setResult(RESULT_PROFILE_UPDATED);
                finish(); // quay lại Fragment
            } else {
                Toast.makeText(this, "Lưu thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút quay lại
        backArrow.setOnClickListener(v -> finish());
    }
}
