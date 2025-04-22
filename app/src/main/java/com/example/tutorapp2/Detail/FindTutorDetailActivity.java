package com.example.tutorapp2.Detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.chat.ChatRoomActivity;
import com.example.tutorapp2.model.FindTutorInfo;

public class FindTutorDetailActivity extends AppCompatActivity {

    private TextView nameText, districtText, subjectsText, salaryText, daysText, noteText;
    private LinearLayout messageLayout;
    private Button btnSendMessage, btnRequestMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_tutor_detail);

        nameText = findViewById(R.id.detailName);
        districtText = findViewById(R.id.detailDistrict);
        subjectsText = findViewById(R.id.detailSubjects);
        salaryText = findViewById(R.id.detailSalary);
        daysText = findViewById(R.id.detailDays);
        noteText = findViewById(R.id.detailNote);

        // 新增：按鈕與區塊初始化
        messageLayout = findViewById(R.id.messageLayout);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnRequestMatch = findViewById(R.id.btnRequestMatch);

        // 新增：讀取登入身份
        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
        String role = prefs.getString("role", ""); // "student" or "parent"
        Log.d("DEBUG_ROLE", "目前登入者角色為：" + role);

        // 顯示或隱藏按鈕區塊
        if (role.equals("student")) {
            messageLayout.setVisibility(View.VISIBLE);
            Log.d("DEBUG_LAYOUT", "學生身分，顯示發送按鈕區塊");
        } else {
            messageLayout.setVisibility(View.GONE);
        }

        // 點擊事件
        btnSendMessage.setOnClickListener(v -> {
            FindTutorInfo info = (FindTutorInfo) getIntent().getSerializableExtra("findTutorInfo");

            if (info != null) {
                int receiverId = info.getUserId();
                String chatName = info.getChildName();

                Intent intent = new Intent(FindTutorDetailActivity.this, ChatRoomActivity.class);
                intent.putExtra("receiverId", receiverId);
                intent.putExtra("chatName", chatName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "找不到對方資訊", Toast.LENGTH_SHORT).show();
            }
        });

        btnRequestMatch.setOnClickListener(v ->
                Toast.makeText(this, "點擊了申請配對", Toast.LENGTH_SHORT).show()
        );

        // 顯示資料
        FindTutorInfo info = (FindTutorInfo) getIntent().getSerializableExtra("findTutorInfo");

        if (info != null) {
            nameText.setText("👶 學員姓名：" + info.getChildName());
            Log.d("DEBUG_DISTRICT", "詳情頁接收到的地區：" + info.getDistrict());
            districtText.setText("📍 地區：" + info.getDistrict());
            subjectsText.setText("📚 科目需求：" + info.getSubjects());
            salaryText.setText("💰 薪資：" + info.getSalary() + " 元 / 小時");
            daysText.setText("📅 輔導時間：" + info.getDays());
            noteText.setText("📝 家教內容：" + info.getNote());
        }
    }
}
