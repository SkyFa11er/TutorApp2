package com.example.tutorapp2.Deal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.Login.LoginActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.adapter.PendingMatchAdapter;
import com.example.tutorapp2.model.MatchItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PendingMatchListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingMatchAdapter adapter;
    private List<MatchItem> matchList = new ArrayList<>();
    private int currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_match_list);

        Log.d("MATCH_DEBUG", "🎯 onCreate 進入了");

        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);
        String token = prefs.getString("token", "");

        Log.d("MATCH_DEBUG", "userId = " + currentUserId + ", token = " + token);

        if (token == null || token.isEmpty() || currentUserId == -1) {
            Toast.makeText(this, "登入資訊異常，請稍後再試", Toast.LENGTH_SHORT).show();
            Log.e("MATCH_DEBUG", "❌ userId = -1 或 token 為空，取消顯示配對通知清單");
            finish(); // ✅ 回上一頁，而非整個跳轉登入
            return;
        }


        recyclerView = findViewById(R.id.recyclerViewPendingMatch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/matches/my")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PendingMatchListActivity.this, "無法取得配對資料", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        runOnUiThread(() -> {
                            Toast.makeText(PendingMatchListActivity.this, "登入過期，請重新登入", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PendingMatchListActivity.this, LoginActivity.class));
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(PendingMatchListActivity.this, "讀取失敗：" + response.code(), Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String body = response.body().string();
                    JSONObject obj = new JSONObject(body);
                    JSONArray arr = obj.getJSONArray("data");
                    matchList.clear();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        String status = item.getString("status");
                        int toUser = item.getInt("user_id");

                        if ("pending".equals(status)) {
                            int matchId = item.getInt("match_id");
                            String name = item.getString("name");
                            String role = item.getString("role");
                            String createdAt = item.getString("created_at").split("T")[0];

                            matchList.add(new MatchItem(matchId, toUser, name, role, createdAt));
                        }
                    }

                    runOnUiThread(() -> {
                        adapter = new PendingMatchAdapter(PendingMatchListActivity.this, matchList);
                        recyclerView.setAdapter(adapter);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}