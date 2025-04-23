package com.example.tutorapp2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.Deal.PendingMatchActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.model.MatchItem;

import java.util.List;

public class PendingMatchAdapter extends RecyclerView.Adapter<PendingMatchAdapter.MatchViewHolder> {

    private Context context;
    private List<MatchItem> matchList;

    public PendingMatchAdapter(Context context, List<MatchItem> matchList) {
        this.context = context;
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem item = matchList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtRole.setText("身份：" + ("student".equals(item.getRole()) ? "學生" : "家長"));
        holder.txtDate.setText("時間：" + item.getDate());

        holder.btnView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PendingMatchActivity.class);
            intent.putExtra("matchId", item.getMatchId());
            intent.putExtra("userId", item.getUserId());
            intent.putExtra("name", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtRole, txtDate;
        Button btnView;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMatchName);
            txtRole = itemView.findViewById(R.id.txtMatchRole);
            txtDate = itemView.findViewById(R.id.txtMatchDate);
            btnView = itemView.findViewById(R.id.btnViewMatch);
        }
    }
}
