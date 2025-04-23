package com.example.tutorapp2.adapter;

import android.content.Context;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.R;
import com.example.tutorapp2.model.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private int currentUserId;
    private Context context;

    public ChatMessageAdapter(Context context, List<ChatMessage> messageList, int currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);
        return (msg.getSenderId() == currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        String content = msg.getContent();

        // 檢查是否為系統配對訊息（JSON 格式）
        if (content.startsWith("{")) {
            try {
                JSONObject json = new JSONObject(content);
                if (json.has("type") && json.getString("type").equals("match_result")) {
                    String text = json.getString("text");
                    holder.textMessage.setText(text);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 一般文字訊息
        holder.textMessage.setText(content);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }
}
