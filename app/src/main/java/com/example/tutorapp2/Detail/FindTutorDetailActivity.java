package com.example.tutorapp2.Detail;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.model.FindTutorInfo;

public class FindTutorDetailActivity extends AppCompatActivity {

    private TextView nameText, districtText, subjectsText, salaryText, daysText, noteText;

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

        FindTutorInfo info = (FindTutorInfo) getIntent().getSerializableExtra("findTutorInfo");

        if (info != null) {
            nameText.setText("👶 學員姓名：" + info.getChildName());
            Log.d("DEBUG_DISTRICT", "詳情頁接收到的地區：" + info.getDistrict());
            districtText.setText("📍 地區：" + info.getDistrict()); // ✅ 新增
            subjectsText.setText("📚 科目需求：" + info.getSubjects());
            salaryText.setText("💰 薪資：" + info.getSalary() + " 元 / 小時");
            daysText.setText("📅 輔導時間：" + info.getDays());
            noteText.setText("📝 家教內容：" + info.getNote());
        }
    }
}

