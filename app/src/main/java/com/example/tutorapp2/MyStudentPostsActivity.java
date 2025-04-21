
package com.example.tutorapp2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.android.jwt.JWT;
import com.example.tutorapp2.adapter.TutorAdapter;
import com.example.tutorapp2.model.TutorInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyStudentPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TutorAdapter adapter;
    private List<TutorInfo> tutorList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private static final String TAG = "STUDENT_MY_POSTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchTutorPosts();
    }

    private void fetchTutorPosts() {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "未登入，請重新登入", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        JWT jwt = new JWT(token);
        String role = jwt.getClaim("role").asString();
        if (!"student".equals(role)) {
            Toast.makeText(this, "非學生身份", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/tutors/my")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MyStudentPostsActivity.this, "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d(TAG, "原始回應資料: " + resBody);

                try {
                    JSONObject root = new JSONObject(resBody);
                    JSONArray array = root.getJSONArray("data");

                    tutorList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        int id = obj.getInt("id");
                        String name = obj.optString("name", "學生");
                        List<String> subjects = Arrays.asList(obj.optString("subjects", "").split(","));
                        String salary = obj.optString("salary", "");
                        String salaryNote = obj.optString("salary_note", "");
                        String intro = obj.optString("intro", "");
                        List<String> days = Arrays.asList(obj.optString("available_days", "").split(","));
                        String startTime = obj.optString("start_time", "");
                        String endTime = obj.optString("end_time", "");

                        tutorList.add(new TutorInfo(id, name, subjects, salary, salaryNote, intro, days, startTime, endTime));
                    }

                    runOnUiThread(() -> {
                        adapter = new TutorAdapter(tutorList, new TutorAdapter.OnItemActionListener() {
                            @Override
                            public void onEdit(TutorInfo item) {
                                Intent intent = new Intent(MyStudentPostsActivity.this, EditTutorActivity.class);
                                intent.putExtra("id", item.getId());
                                intent.putExtra("name", item.getName());
                                intent.putExtra("subjects", String.join(",", item.getSubjects()));
                                intent.putExtra("salary", item.getSalary());
                                intent.putExtra("salary_note", item.getSalaryNote());
                                intent.putExtra("intro", item.getIntro());
                                intent.putExtra("available_days", String.join(",", item.getAvailableDays()));
                                intent.putExtra("start_time", item.getStartTime());
                                intent.putExtra("end_time", item.getEndTime());
                                startActivityForResult(intent, 1001);
                            }

                            @Override
                            public void onDelete(TutorInfo item) {
                                new AlertDialog.Builder(MyStudentPostsActivity.this)
                                        .setTitle("刪除確認")
                                        .setMessage("確定要刪除這筆家教資料嗎？")
                                        .setPositiveButton("刪除", (dialog, which) -> deleteTutorPostById(item.getId(), token))
                                        .setNegativeButton("取消", null)
                                        .show();
                            }
                        },true);
                        recyclerView.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    Log.e(TAG, "解析 JSON 錯誤：" + e.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(MyStudentPostsActivity.this, "資料解析失敗", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteTutorPostById(int id, String token) {
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/tutors/" + id)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MyStudentPostsActivity.this, "刪除失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MyStudentPostsActivity.this, "✅ 已刪除", Toast.LENGTH_SHORT).show();
                        fetchTutorPosts(); // 重新加載
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MyStudentPostsActivity.this, "刪除失敗：" + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            fetchTutorPosts();
        }
    }


}
