package com.example.nesvie_copyzalo.Models;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.nesvie_copyzalo.AddUser;
import com.example.nesvie_copyzalo.DBHelper;
import com.example.nesvie_copyzalo.Fragments.Fragment_canhan;
import com.example.nesvie_copyzalo.Fragments.Fragment_danhba;
import com.example.nesvie_copyzalo.Fragments.Fragment_tuongnha;
import com.example.nesvie_copyzalo.Fragments.Fragment_tinnhan;
import com.example.nesvie_copyzalo.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private String role; // giữ role của user đăng nhập
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Lấy role từ Sign_in
        role = getIntent().getStringExtra("role");
        if (role == null) role = "user"; // mặc định user

        navigationView = findViewById(R.id.bottom_nev);

        // Mặc định mở fragment tin nhắn
        replaceFragment(new Fragment_tinnhan());

        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_tinhan) {
                    replaceFragment(new Fragment_tinnhan());
                    return true;
                } else if (id == R.id.action_danhba) {
                    replaceFragment(new Fragment_danhba());
                    return true;
                } else if (id == R.id.action_tuongnha) {
                    replaceFragment(new Fragment_tuongnha());
                    return true;
                } else if (id == R.id.action_canhan) {
                    // Truyền role vào Fragment_canhan
                    Fragment_canhan fragment = new Fragment_canhan();
                    Bundle args = new Bundle();
                    args.putString("role", role);
                    fragment.setArguments(args);
                    replaceFragment(fragment);
                    return true;
                }
                return false;
            }
        });

        // 👉 Thêm xử lý click cho icon thêm bạn (note)
        ImageView note = findViewById(R.id.note);
        note.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddUser.class);
            startActivity(intent);
        });

        // Cập nhật badge lần đầu
        updateUnreadBadge();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    // ====== Hàm cập nhật badge tin nhắn ======
    private void updateUnreadBadge() {
        DBHelper dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String currentUserId = prefs.getString("current_user_id", null);

        if (currentUserId == null) return;

        // Đếm số tin nhắn chưa đọc
        int unreadCount = dbHelper.getTotalUnreadCount(currentUserId);

        BadgeDrawable badge = navigationView.getOrCreateBadge(R.id.action_tinhan);
        badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        badge.setBadgeTextColor(getResources().getColor(android.R.color.white));

        if (unreadCount > 0) {
            badge.setVisible(true);
            if (unreadCount > 5) {
                badge.setNumber(5); // hiển thị 5
                badge.setContentDescriptionNumberless("5+"); // để screen reader đọc "5+"
            } else {
                badge.setNumber(unreadCount);
            }
        } else {
            badge.setVisible(false);
        }
        // ===== Badge danh bạ =====
        int pendingCount = dbHelper.getPendingFriendRequestCount(currentUserId); // hàm này cũng tự viết
        BadgeDrawable friendBadge = navigationView.getOrCreateBadge(R.id.action_danhba);
        if (pendingCount > 0) {
            friendBadge.setVisible(true);
            friendBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            friendBadge.setBadgeTextColor(getResources().getColor(android.R.color.white));
            friendBadge.setNumber(pendingCount > 5 ? 5 : pendingCount);
            if (pendingCount > 5) friendBadge.setText("5+");
        } else {
            friendBadge.setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadBadge(); // reload badge mỗi khi quay lại màn hình chính
    }
}
