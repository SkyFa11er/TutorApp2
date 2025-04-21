package com.example.tutorapp2;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.android.jwt.JWT;
import com.example.tutorapp2.adapter.FindTutorAdapter;
import com.example.tutorapp2.model.FindTutorInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import okhttp3.*;

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FindTutorAdapter adapter;
    private List<FindTutorInfo> postList = new ArrayList<>();
    private static final String TAG = "MY_POSTS";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "未登入，請重新登入", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        JWT jwt = new JWT(token);
        String role = jwt.getClaim("role").asString();

        String apiUrl;
        if ("student".equals(role)) {
            apiUrl = "http://8.138.229.36:3000/api/tutors/my";
        } else if ("parent".equals(role)) {
            apiUrl = "http://8.138.229.36:3000/api/find-tutors/my";
        } else {
            Toast.makeText(this, "角色不明，無法獲取資料", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MyPostsActivity.this, "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d(TAG, "伺服器回應：" + resBody);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(resBody);
                        postList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("id");
                            String name = obj.optString("child_name", "（無名）");
                            String subjects = obj.optString("subjects", "");
                            int salary = obj.optInt("salary", 0);
                            String days = obj.optString("days", "");
                            String note = obj.optString("note", "");
                            String phone = obj.optString("phone", "");
                            String district = obj.optString("district", "");
                            String address = obj.optString("address", "");

                            postList.add(new FindTutorInfo(id, name, subjects, salary, days, note, phone, district, address));
                        }

                        runOnUiThread(() -> {
                            adapter = new FindTutorAdapter(postList, new FindTutorAdapter.OnItemActionListener() {
                                @Override
                                public void onEdit(FindTutorInfo item) {
                                    Intent intent = new Intent(MyPostsActivity.this, EditFindTutorActivity.class);
                                    intent.putExtra("id", item.getId());
                                    intent.putExtra("child_name", item.getChildName());
                                    intent.putExtra("subjects", item.getSubjects());
                                    intent.putExtra("salary", item.getSalary());
                                    intent.putExtra("days", item.getDays());
                                    intent.putExtra("note", item.getNote());
                                    intent.putExtra("phone", item.getPhone());
                                    intent.putExtra("district", item.getDistrict());
                                    intent.putExtra("address", item.getAddress());
                                    startActivityForResult(intent, 1001);
                                }

                                @Override
                                public void onDelete(FindTutorInfo item) {
                                    new AlertDialog.Builder(MyPostsActivity.this)
                                            .setTitle("確認刪除")
                                            .setMessage("確定要刪除「" + item.getChildName() + "」這筆資料嗎？")
                                            .setPositiveButton("刪除", (dialog, which) -> {
                                                deletePostById(item.getId());
                                            })
                                            .setNegativeButton("取消", null)
                                            .show();
                                }
                            }, true);
                            recyclerView.setAdapter(adapter);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(MyPostsActivity.this, "解析資料失敗", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MyPostsActivity.this, "伺服器錯誤：" + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void deletePostById(int id) {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String url = "http://8.138.229.36:3000/api/find-tutors/" + id;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MyPostsActivity.this, "刪除失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(MyPostsActivity.this, "刪除成功", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(MyPostsActivity.this, "刪除失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadData();
        }
    }
}
