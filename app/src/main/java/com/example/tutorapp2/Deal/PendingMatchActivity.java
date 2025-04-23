package com.example.tutorapp2.Deal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PendingMatchActivity extends AppCompatActivity {

    private int matchId;
    private int otherUserId;
    private String otherUserName;

    private TextView txtTitle;
    private Button btnAccept;
    private Button btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_match);

        txtTitle = findViewById(R.id.txtMatchTitle);
        btnAccept = findViewById(R.id.btnAcceptMatch);
        btnReject = findViewById(R.id.btnRejectMatch);

        matchId = getIntent().getIntExtra("matchId", -1);
        otherUserId = getIntent().getIntExtra("userId", -1);
        otherUserName = getIntent().getStringExtra("name");

        txtTitle.setText("收到來自「" + otherUserName + "」的配對請求");

        btnAccept.setOnClickListener(v -> handleMatchAction("accept"));
        btnReject.setOnClickListener(v -> handleMatchAction("reject"));
    }

    private void handleMatchAction(String action) {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/matches/" + matchId + "/" + action)
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PendingMatchActivity.this, "操作失敗", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(PendingMatchActivity.this, "已完成配對處理", Toast.LENGTH_SHORT).show());
                    if (action.equals("accept")) {
                        sendSystemMessage(otherUserId, matchId, "🎉 你已通過配對申請，請開始聯繫吧！");
                    } else if (action.equals("reject")) {
                        sendSystemMessage(otherUserId, matchId, "❌ 很抱歉，對方拒絕了配對申請。請尋找其他對象喔！");
                    }
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(PendingMatchActivity.this, "配對處理失敗", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void sendSystemMessage(int receiverId, int matchId, String text) {
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        String name = prefs.getString("name", "對方");

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        JSONObject content = new JSONObject();
        try {
            content.put("type", "match_result");
            content.put("match_id", matchId);
            content.put("text", text);
            content.put("from_name", name);

            json.put("receiver_id", receiverId);
            json.put("content", content.toString());
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

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { Log.e("SYS_MSG", "系統訊息發送失敗", e); }
            @Override public void onResponse(Call call, Response response) { Log.d("SYS_MSG", "系統訊息發送成功"); }
        });
    }
}
