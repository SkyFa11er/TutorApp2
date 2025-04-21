package com.example.tutorapp2.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auth0.android.jwt.JWT;
import com.example.tutorapp2.Login.LoginActivity;
import com.example.tutorapp2.MyPostsActivity;
import com.example.tutorapp2.MyStudentPostsActivity;
import com.example.tutorapp2.R;

public class ProfileFragment extends Fragment {

    private TextView textUserName;
    private TextView textUserRole;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Context context = requireContext(); // ✅ 提早取得 context

        textUserName = view.findViewById(R.id.textUserName);
        textUserRole = view.findViewById(R.id.textUserRole);
        Button buttonMyPosts = view.findViewById(R.id.buttonMyPosts);
        Button buttonLogout = view.findViewById(R.id.buttonLogout);

        SharedPreferences prefs = context.getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token != null) {
            try {
                JWT jwt = new JWT(token);
                String name = jwt.getClaim("name").asString();
                String role = jwt.getClaim("role").asString();

                textUserName.setText(name != null ? name : "未知使用者");
                textUserRole.setText("student".equals(role) ? "學生" : "家長");

            } catch (Exception e) {
                e.printStackTrace();
                textUserName.setText("解析失敗");
                textUserRole.setText("未知身份");
            }
        } else {
            textUserName.setText("未登入");
            textUserRole.setText("訪客");
        }

        // 我的發布（功能預留）
        buttonMyPosts.setOnClickListener(v -> {
            if (token != null) {
                JWT jwt = new JWT(token);
                String role = jwt.getClaim("role").asString();

                Intent intent;
                if ("student".equals(role)) {
                    intent = new Intent(context, MyStudentPostsActivity.class);
                } else {
                    intent = new Intent(context, MyPostsActivity.class);
                }
                startActivity(intent);
            } else {
                Toast.makeText(context, "尚未登入", Toast.LENGTH_SHORT).show();
            }
        });



        // 登出並跳轉回登入頁面
        buttonLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("token");
            editor.apply();

            Toast.makeText(context, "已登出", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // 關閉 MainActivity，避免返回
        });

        return view;
    }
}
