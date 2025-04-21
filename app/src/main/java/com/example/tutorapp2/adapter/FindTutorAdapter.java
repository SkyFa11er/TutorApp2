package com.example.tutorapp2.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.Detail.FindTutorDetailActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.model.FindTutorInfo;

import java.util.List;

public class FindTutorAdapter extends RecyclerView.Adapter<FindTutorAdapter.FindTutorViewHolder> {

    private List<FindTutorInfo> dataList;
    private boolean showButtons;

    public interface OnItemActionListener {
        void onEdit(FindTutorInfo item);
        void onDelete(FindTutorInfo item);
    }

    private OnItemActionListener listener;

    public FindTutorAdapter(List<FindTutorInfo> dataList, OnItemActionListener listener, boolean showButtons) {
        this.dataList = dataList;
        this.listener = listener;
        this.showButtons = showButtons;
    }

    @NonNull
    @Override
    public FindTutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_find_tutor, parent, false);
        return new FindTutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindTutorViewHolder holder, int position) {
        FindTutorInfo info = dataList.get(position);
        Context context = holder.itemView.getContext(); // ✅ 取得 context

        holder.textChildName.setText("👧 " + info.getChildName());
        holder.textSubjects.setText("📚 科目：" + info.getSubjects());
        holder.textSalary.setText("💰 薪資：" + info.getSalary() + " 元 / 小時");
        holder.textDays.setText("📅 輔導時間：" + info.getDays());
        holder.textNote.setText("📝 備註：" + info.getNote());

        // ✅ 正確方式：放在點擊事件中
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FindTutorDetailActivity.class);
            intent.putExtra("findTutorInfo", info);
            Log.d("DEBUG_DISTRICT", "點擊卡片地區：" + info.getDistrict());
            context.startActivity(intent);
        });

        if (showButtons) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(info));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(info));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class FindTutorViewHolder extends RecyclerView.ViewHolder {
        TextView textChildName, textSubjects, textSalary, textDays, textNote;
        Button btnEdit, btnDelete;

        public FindTutorViewHolder(@NonNull View itemView) {
            super(itemView);
            textChildName = itemView.findViewById(R.id.textChildName);
            textSubjects = itemView.findViewById(R.id.textSubjects);
            textSalary = itemView.findViewById(R.id.textSalary);
            textDays = itemView.findViewById(R.id.textDays);
            textNote = itemView.findViewById(R.id.textNote);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
