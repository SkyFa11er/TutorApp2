package com.example.tutorapp2.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.tutorapp2.R;
import com.example.tutorapp2.adapter.ChatAdapter;
import com.example.tutorapp2.model.ChatItem;
import okhttp3.*;
import org.json.*;

import java.io.IOException;
import java.util.*;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter(getContext(), chatList);
        recyclerView.setAdapter(adapter);

        loadConversations();
        return view;
    }

    private void loadConversations() {
        SharedPreferences prefs = requireContext().getSharedPreferences("TutorAppPrefs", getContext().MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/messages/conversations")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "聊天室載入失敗", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    chatList.clear();
                    try {
                        String resBody = response.body().string();
                        JSONObject json = new JSONObject(resBody);
                        JSONArray array = json.getJSONArray("data");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int userId = obj.getInt("user_id");
                            String name = obj.optString("name", "未知使用者");
                            String lastMessage = obj.optString("last_message", "");
                            String timestamp = obj.optString("timestamp", "");

                            if (lastMessage.contains("\"type\":\"match_result\"")) {
                                try {
                                    JSONObject sysMsg = new JSONObject(lastMessage);
                                    String status = sysMsg.optString("status");

                                    if ("active".equals(status)) {
                                        lastMessage = "🎉 配對成功通知，請開始聯繫！";
                                    } else if ("rejected".equals(status)) {
                                        lastMessage = "❌ 很抱歉，對方拒絕了配對申請。請尋找其他對象唷！";
                                    } else if ("closed".equals(status)) {
                                        lastMessage = "⚠️ 該配對已結束";
                                    } else {
                                        lastMessage = "📌 配對狀態更新通知";
                                    }
                                } catch (JSONException e) {
                                    lastMessage = "📌 系統通知";
                                }
                            } else if (lastMessage.contains("\"type\":\"match_request\"")) {
                                lastMessage = "📩 有人申請與你配對，請盡快處理～";
                            }


                            chatList.add(new ChatItem(userId, name, lastMessage, timestamp));
                        }


                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
