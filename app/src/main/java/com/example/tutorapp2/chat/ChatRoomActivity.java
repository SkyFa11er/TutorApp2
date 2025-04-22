package com.example.tutorapp2.chat;

import android.content.SharedPreferences;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.tutorapp2.R;
import com.example.tutorapp2.adapter.ChatMessageAdapter;
import com.example.tutorapp2.model.ChatMessage;
import org.json.*;

import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class ChatRoomActivity extends AppCompatActivity {

    private int receiverId;
    private String chatName;
    private int currentUserId;

    private TextView chatTitle;
    private EditText editMessage;
    private Button btnSend;
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    private final Handler handler = new Handler();
    private final OkHttpClient client = new OkHttpClient();

    private final Runnable pollMessages = new Runnable() {
        @Override
        public void run() {
            loadMessages();
            handler.postDelayed(this, 3000); // 每 3 秒輪詢一次
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 初始化
        chatTitle = findViewById(R.id.chatTitle);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerView); // ← 你要在 XML 加這個 RecyclerView

        // 接收傳入資料
        receiverId = getIntent().getIntExtra("receiverId", -1);
        chatName = getIntent().getStringExtra("chatName");

        chatTitle.setText("與 " + chatName + " 的對話");

        // 取得自己 ID
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);
        String token = prefs.getString("token", "");

        // RecyclerView 設定
        adapter = new ChatMessageAdapter(this, messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 發送按鈕
        btnSend.setOnClickListener(v -> {
            String content = editMessage.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "請輸入訊息", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(content, token);
        });

        // 啟動輪詢
        handler.post(pollMessages);
    }

    private void loadMessages() {
        String url = "http://8.138.229.36:3000/api/messages/chat/" + receiverId;
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    messageList.clear();
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        JSONArray array = json.getJSONArray("data");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            ChatMessage msg = new ChatMessage(
                                    obj.getInt("sender_id"),
                                    obj.getInt("receiver_id"),
                                    obj.getString("content"),
                                    obj.getString("timestamp")
                            );
                            messageList.add(msg);
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendMessage(String content, String token) {
        String url = "http://8.138.229.36:3000/api/messages/send";

        JSONObject json = new JSONObject();
        try {
            json.put("receiver_id", receiverId);
            json.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        editMessage.setText("");
                        loadMessages(); // 重新撈一次（立即更新）
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollMessages); // 停止輪詢
    }
}
