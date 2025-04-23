package com.example.tutorapp2.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tutorapp2.R;
import com.example.tutorapp2.chat.ChatRoomActivity;
import com.example.tutorapp2.model.ChatItem;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatItem> chatList;

    public ChatAdapter(Context context, List<ChatItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }
    private String formatTimestamp(String rawTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date messageDate = inputFormat.parse(rawTime);
            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(messageDate);

            Calendar now = Calendar.getInstance();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            if (isSameDay(messageCal, now)) {
                return "今天 " + timeFormat.format(messageDate);
            }

            now.add(Calendar.DATE, -1);
            if (isSameDay(messageCal, now)) {
                return "昨天 " + timeFormat.format(messageDate);
            }

            return fullDateFormat.format(messageDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return rawTime;
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void deleteConversation(int receiverId, int position) {
        SharedPreferences prefs = context.getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/messages/conversation/" + receiverId)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "刪除失敗", Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> {
                        chatList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "已刪除對話", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }



    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem item = chatList.get(position);
        holder.textName.setText(item.getName());
        holder.textLastMessage.setText(item.getLastMessage());
        holder.textTimestamp.setText(formatTimestamp(item.getTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("receiverId", item.getUserId());
            intent.putExtra("chatName", item.getName());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("刪除聊天室")
                    .setMessage("是否要刪除與 " + item.getName() + " 的所有訊息？")
                    .setPositiveButton("刪除", (dialog, which) -> {
                        deleteConversation(item.getUserId(), position);
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textLastMessage, textTimestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}
