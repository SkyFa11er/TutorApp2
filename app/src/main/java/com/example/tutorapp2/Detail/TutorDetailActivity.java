package com.example.tutorapp2.Detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.chat.ChatRoomActivity;
import com.example.tutorapp2.model.TutorInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TutorDetailActivity extends AppCompatActivity {

    private TextView nameText, subjectsText, salaryText, salaryNoteText,
            introText, daysText, timeText;

    private LinearLayout messageLayout;
    private Button btnSendMessage, btnRequestMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_detail);

        nameText = findViewById(R.id.detailName);
        subjectsText = findViewById(R.id.detailSubjects);
        salaryText = findViewById(R.id.detailSalary);
        salaryNoteText = findViewById(R.id.detailSalaryNote);
        introText = findViewById(R.id.detailIntro);
        daysText = findViewById(R.id.detailDays);
        timeText = findViewById(R.id.detailTime);

        messageLayout = findViewById(R.id.messageLayout);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnRequestMatch = findViewById(R.id.btnRequestMatch);

        // ✅ 取得登入者身份
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String role = prefs.getString("role", "");

        if (role.equals("parent")) {
            messageLayout.setVisibility(View.VISIBLE);
        } else {
            messageLayout.setVisibility(View.GONE);
        }

        // 點擊事件
        btnSendMessage.setOnClickListener(v -> {
            TutorInfo tutor = (TutorInfo) getIntent().getSerializableExtra("tutorInfo");

            if (tutor != null) {
                int receiverId = tutor.getUserId();
                String chatName = tutor.getName();

                Intent intent = new Intent(TutorDetailActivity.this, ChatRoomActivity.class);
                intent.putExtra("receiverId", receiverId);
                intent.putExtra("chatName", chatName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "找不到對方資訊", Toast.LENGTH_SHORT).show();
            }
        });


        btnRequestMatch.setOnClickListener(v -> {
            TutorInfo tutor = (TutorInfo) getIntent().getSerializableExtra("tutorInfo");

            if (tutor != null) {
                String token = prefs.getString("token", "");
                int toUserId = tutor.getUserId();

                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                try {
                    json.put("to_user", toUserId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("http://8.138.229.36:3000/api/matches")
                        .addHeader("Authorization", "Bearer " + token)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(TutorDetailActivity.this, "申請配對失敗", Toast.LENGTH_SHORT).show());
                    }

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(() ->
                                    Toast.makeText(TutorDetailActivity.this, "配對申請已送出", Toast.LENGTH_SHORT).show());

                            // 發送系統訊息通知
                            sendSystemMessage(toUserId, "📩 配對申請已送出，請等待對方確認。");
                        } else {
                            String err = response.body().string();
                            runOnUiThread(() ->
                                    Toast.makeText(TutorDetailActivity.this, "配對失敗：" + err, Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        });


        TutorInfo tutor = (TutorInfo) getIntent().getSerializableExtra("tutorInfo");

        if (tutor != null) {
            nameText.setText("👨‍🎓 姓名：" + tutor.getName());
            subjectsText.setText("📚 教授科目：" + listToString(tutor.getSubjects()));
            salaryText.setText("💰 薪資：" + tutor.getSalary() + " 元 / 小時");
            salaryNoteText.setText("📝 薪資備註：" + tutor.getSalaryNote());
            introText.setText("🧾 自我介紹：" + tutor.getIntro());
            daysText.setText("📅 可教學日：" + listToString(tutor.getAvailableDays()));
            timeText.setText("⏰ 時段：" + tutor.getStartTime() + " ～ " + tutor.getEndTime());
        }
    }

    private String listToString(List<String> list) {
        return list != null ? String.join("、", list) : "";
    }

    private void sendSystemMessage(int receiverId, String message) {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("receiver_id", receiverId);
            json.put("content", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/messages/send")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                // 可以選擇不提示
            }

            @Override public void onResponse(Call call, Response response) {
                // 可以加上 Log 記錄訊息發送成功
            }
        });
    }

}
