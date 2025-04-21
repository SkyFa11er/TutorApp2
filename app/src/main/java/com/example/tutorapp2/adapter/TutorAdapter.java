package com.example.tutorapp2.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.Detail.TutorDetailActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.model.TutorInfo;

import java.util.List;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEdit(TutorInfo item);
        void onDelete(TutorInfo item);
    }

    private final List<TutorInfo> tutorList;
    private final OnItemActionListener listener;
    private final boolean showActions; // ✅ 新增：是否顯示編輯/刪除

    public TutorAdapter(List<TutorInfo> tutorList, OnItemActionListener listener, boolean showActions) {
        this.tutorList = tutorList;
        this.listener = listener;
        this.showActions = showActions;
    }

    @NonNull
    @Override
    public TutorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutor_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorAdapter.ViewHolder holder, int position) {
        TutorInfo item = tutorList.get(position);
        Context context = holder.itemView.getContext();

        holder.nameText.setText("👩‍🏫 " + item.getName());
        holder.subjectsText.setText("📚 科目：" + item.getSubjects());
        holder.salaryText.setText("💰 薪資：" + item.getSalary() + " 元 / 小時");
        holder.daysText.setText("📅 輔導時間：" + item.getAvailableDays());
        holder.introText.setText("📝 備註：" + item.getIntro());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TutorDetailActivity.class);
            intent.putExtra("tutorInfo", item); // TutorInfo 需實作 Serializable
            context.startActivity(intent);
        });

        if (showActions && listener != null) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("確認刪除")
                        .setMessage("確定要刪除「" + item.getName() + "」這筆資料嗎？")
                        .setPositiveButton("刪除", (dialog, which) -> listener.onDelete(item))
                        .setNegativeButton("取消", null)
                        .show();
            });
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tutorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, subjectsText, salaryText, daysText, introText;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textName);
            subjectsText = itemView.findViewById(R.id.textSubjects);
            salaryText = itemView.findViewById(R.id.textSalary);
            daysText = itemView.findViewById(R.id.textDays);
            introText = itemView.findViewById(R.id.textIntro);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
