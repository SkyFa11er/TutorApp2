package com.example.tutorapp2.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auth0.android.jwt.JWT;
import com.example.tutorapp2.Deal.PendingMatchListActivity;
import com.example.tutorapp2.Login.LoginActivity;
import com.example.tutorapp2.MyPostsActivity;
import com.example.tutorapp2.MyStudentPostsActivity;
import com.example.tutorapp2.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private TextView textUserName;
    private TextView textUserRole;
    private TextView txtMatchInfo, txtMatchedPhone, txtMatchedDistrict, txtMatchedAddress;
    private LinearLayout matchInfoLayout;
    private Button btnEndMatch;
    private String token;
    private int currentUserId;
    private int currentMatchId = -1;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Context context = requireContext();

        txtMatchedPhone = view.findViewById(R.id.txtMatchedPhone);
        txtMatchedDistrict = view.findViewById(R.id.txtMatchedDistrict);
        txtMatchedAddress = view.findViewById(R.id.txtMatchedAddress);
        textUserName = view.findViewById(R.id.textUserName);
        textUserRole = view.findViewById(R.id.textUserRole);
        txtMatchInfo = view.findViewById(R.id.txtMatchInfo);
        matchInfoLayout = view.findViewById(R.id.matchInfoLayout);
        btnEndMatch = view.findViewById(R.id.btnEndMatch);
        matchInfoLayout.setVisibility(View.GONE);

        txtMatchedPhone.setVisibility(View.GONE);
        txtMatchedDistrict.setVisibility(View.GONE);
        txtMatchedAddress.setVisibility(View.GONE);
        btnEndMatch.setVisibility(View.GONE);

        Button buttonMyPosts = view.findViewById(R.id.buttonMyPosts);
        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        Button buttonMatchList = view.findViewById(R.id.buttonMatchList);

        SharedPreferences prefs = context.getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);
        currentUserId = prefs.getInt("userId", -1);

        if (token != null) {
            try {
                JWT jwt = new JWT(token);
                String name = jwt.getClaim("name").asString();
                String role = jwt.getClaim("role").asString();

                textUserName.setText(name != null ? name : "未知使用者");
                textUserRole.setText("student".equals(role) ? "學生" : "家長");

                checkMatchSuccess(context, role);

            } catch (Exception e) {
                e.printStackTrace();
                textUserName.setText("解析失敗");
                textUserRole.setText("未知身份");
            }
        } else {
            textUserName.setText("未登入");
            textUserRole.setText("訪客");
        }

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

        buttonMatchList.setOnClickListener(v -> {
            Intent intent = new Intent(context, PendingMatchListActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("token");
            editor.apply();

            Toast.makeText(context, "已登出", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        btnEndMatch.setOnClickListener(v -> {
            if (currentMatchId != -1) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://8.138.229.36:3000/api/matches/" + currentMatchId + "/close")
                        .put(RequestBody.create(null, new byte[0]))
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "⚠️ 結束配對失敗", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "✅ 已成功結束配對", Toast.LENGTH_SHORT).show();
                            matchInfoLayout.setVisibility(View.GONE);
                        });
                    }
                });
            }
        });

        return view;
    }

    private void checkMatchSuccess(Context context, String role) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/matches/my")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("PROFILE", "配對查詢失敗");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject obj = new JSONObject(body);
                    JSONArray arr = obj.getJSONArray("data");

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        String status = item.getString("status");
                        String matchName = item.getString("name");
                        String matchRole = item.getString("role");
                        String matchPhone = item.optString("phone", "未提供");
                        String matchDistrict = item.optString("district", "未提供");
                        String matchAddress = item.optString("address", "未提供");
                        int matchId = item.getInt("match_id");

                        if ("active".equals(status)) {
                            currentMatchId = matchId;
                            String info = "您已與 " + matchName + " 配對成功！";

                            requireActivity().runOnUiThread(() -> {
                                matchInfoLayout.setVisibility(View.VISIBLE);
                                txtMatchInfo.setText(info);
                                btnEndMatch.setVisibility(View.VISIBLE);

                                // 根據對方的身份與是否有地址，判斷顯示內容
                                if ("parent".equals(matchRole)) {
                                    if (!"未提供".equals(matchAddress)) {
                                        // 有地址 → 學生主動配對家長
                                        txtMatchedDistrict.setText("📍 家長地區：" + matchDistrict);
                                        txtMatchedAddress.setText("🏠 詳細地址：" + matchAddress);
                                        txtMatchedDistrict.setVisibility(View.VISIBLE);
                                        txtMatchedAddress.setVisibility(View.VISIBLE);
                                        txtMatchedPhone.setVisibility(View.GONE);
                                    } else {
                                        // 無地址 → 家長主動配對學生
                                        txtMatchedPhone.setText("📞 家長電話：" + matchPhone);
                                        txtMatchedPhone.setVisibility(View.VISIBLE);
                                        txtMatchedDistrict.setVisibility(View.GONE);
                                        txtMatchedAddress.setVisibility(View.GONE);
                                    }
                                } else {
                                    // 對方是學生 → 顯示學生電話
                                    txtMatchedPhone.setText("📞 學生電話：" + matchPhone);
                                    txtMatchedPhone.setVisibility(View.VISIBLE);
                                    txtMatchedDistrict.setVisibility(View.GONE);
                                    txtMatchedAddress.setVisibility(View.GONE);
                                }
                            });
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
