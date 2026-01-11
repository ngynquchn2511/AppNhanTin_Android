package com.example.nesvie_copyzalo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Sign_up extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtTenHienThi;
    private Button btnDangKy;
    private ImageView backArrow;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtTenHienThi = findViewById(R.id.edtTenHienThi);
        btnDangKy = findViewById(R.id.btnDangKy);
        backArrow = findViewById(R.id.backArrow);

        dbHelper = new DBHelper(this);

        backArrow.setOnClickListener(v -> finish());

        btnDangKy.setOnClickListener(v -> dangKy());
    }

    private void dangKy() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String tenHienThi = edtTenHienThi.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        User newUser = new User(
                null,             // id sẽ được set sau khi insert
                email,
                tenHienThi,
                "user",           // role mặc định
                "khác",           // giới tính mặc định
                password,
                now,
                now
        );

        long userId = dbHelper.insertUser(newUser);

        if (userId > 0) {
            newUser.setId(String.valueOf(userId)); // ✅ gán id mới vào User

            // lưu session
            getSharedPreferences("user_session", MODE_PRIVATE)
                    .edit()
                    .putString("current_user_id", newUser.getId())
                    .apply();

            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Sign_up.this, Sign_in.class));
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}
