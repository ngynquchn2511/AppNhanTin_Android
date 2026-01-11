package com.example.nesvie_copyzalo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nesvie_copyzalo.Models.MainActivity;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Sign_in extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnDangNhap;
    private ImageView backArrow;
    private TextView txtDangKy;

    private DBHelper dbHelper;
    private Socket mSocket;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        txtDangKy = findViewById(R.id.txtDangKy);
        backArrow = findViewById(R.id.backArrow);

        dbHelper = new DBHelper(this);

        // ✅ Thống nhất dùng "user_session"
        prefs = getSharedPreferences("user_session", MODE_PRIVATE);

        // Kết nối Socket (nếu có server)
        try {
            mSocket = IO.socket("http://YOUR_SERVER_IP:3000"); // đổi IP server
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnDangNhap.setOnClickListener(v -> dangNhap());
        txtDangKy.setOnClickListener(v -> {
            startActivity(new Intent(Sign_in.this, Sign_up.class));
        });
        backArrow.setOnClickListener(v -> finish());
    }

    private void dangNhap() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserByEmailAndPassword(email, password);
        if (user != null) {
            // ✅ Lưu session
            prefs.edit()
                    .putString("current_user_id", user.getId())
                    .apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Sign_in.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
        }
    }
}
