package com.example.nesvie_copyzalo.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nesvie_copyzalo.AddUser;
import com.example.nesvie_copyzalo.ChangePasswordActivity;
import com.example.nesvie_copyzalo.DBHelper;
import com.example.nesvie_copyzalo.LichSuDangNhapActivity;
import com.example.nesvie_copyzalo.Models.Profile;
import com.example.nesvie_copyzalo.R;
import com.example.nesvie_copyzalo.Sign_in;
import com.example.nesvie_copyzalo.User;

public class Fragment_canhan extends Fragment {

    private TextView tvRole, tvThongTin, tvBaoCao;
    private Button btnDangXuat;
    private LinearLayout layoutBaoCao, layoutThemBan, layoutDoiMatKhau, layoutThoiGianHoatDong, layoutLichSuDangNhap;

    private DBHelper dbHelper;
    private String currentUserId;
    private ActivityResultLauncher<Intent> profileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_canhan, container, false);

        // Ánh xạ view
        tvRole = view.findViewById(R.id.tvRole);
        tvThongTin = view.findViewById(R.id.ThongTinCaNhan);
        btnDangXuat = view.findViewById(R.id.btnDangXuat);
        layoutBaoCao = view.findViewById(R.id.layoutBaoCao);
        tvBaoCao = view.findViewById(R.id.XemBaoCao);
        layoutThemBan = view.findViewById(R.id.layoutThemBan);
        layoutDoiMatKhau = view.findViewById(R.id.layoutDoiMatKhau);
        layoutThoiGianHoatDong = view.findViewById(R.id.layoutThoiGianHoatDong);
        layoutLichSuDangNhap = view.findViewById(R.id.layoutLichSuDangNhap);

        dbHelper = new DBHelper(requireContext());

        // Lấy currentUserId từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", requireContext().MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_id", null);
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), Sign_in.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return view;
        }

        loadUserData();

        profileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Profile.RESULT_PROFILE_UPDATED) {
                        loadUserData(); // Cập nhật tên hiển thị
                    }
                }
        );

        setupClickListeners();

        return view;
    }

    private void loadUserData() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(requireContext(), "Lỗi: Không có ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            User currentUser = dbHelper.getUserById(currentUserId);
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị quyền
            String role = currentUser.getRole();
            if (!TextUtils.isEmpty(role)) {
                tvRole.setText("Quyền: " + role);
                layoutBaoCao.setVisibility("admin".equalsIgnoreCase(role.trim()) ? View.VISIBLE : View.GONE);
            } else {
                tvRole.setText("Quyền: Người dùng");
                layoutBaoCao.setVisibility(View.GONE);
            }

            // Hiển thị tên hiển thị hoặc email
            String displayText = "Thông tin cá nhân";
            if (!TextUtils.isEmpty(currentUser.getTenHienThi())) {
                displayText = currentUser.getTenHienThi();
            } else if (!TextUtils.isEmpty(currentUser.getEmail())) {
                displayText = currentUser.getEmail();
            }
            tvThongTin.setText(displayText);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi khi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Mở Profile
        tvThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Profile.class);
            intent.putExtra("user_id", currentUserId);
            profileLauncher.launch(intent);
        });

        // Mở AddUser
        layoutThemBan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddUser.class);
            startActivity(intent);
        });

        // Mở Reports (chỉ admin)
        tvBaoCao.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.nesvie_copyzalo.Reports.Reports.class);
            startActivity(intent);
        });

        // Mở ChangePasswordActivity
        layoutDoiMatKhau.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Mở Lịch sử đăng nhập
        layoutLichSuDangNhap.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LichSuDangNhapActivity.class);
            startActivity(intent);
        });

        layoutThoiGianHoatDong.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.nesvie_copyzalo.ActivityChartActivity.class);
            intent.putExtra("userId", currentUserId); // truyền userId để lấy dữ liệu
            startActivity(intent);
        });
        // Đăng xuất
        btnDangXuat.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", requireContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("current_user_id");
                        editor.apply();

                        Intent intent = new Intent(getActivity(), Sign_in.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
