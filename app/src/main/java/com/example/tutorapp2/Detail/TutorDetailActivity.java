package com.example.tutorapp2.Detail;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tutorapp2.R;
import com.example.tutorapp2.model.TutorInfo;

import java.util.List;

public class TutorDetailActivity extends AppCompatActivity {

    private TextView nameText, subjectsText, salaryText, salaryNoteText,
            introText, daysText, timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_detail);

        nameText = findViewById(R.id.detailName);
        subjectsText = findViewById(R.id.detailSubjects);
        salaryText = findViewById(R.id.detailSalary);
        salaryNoteText = findViewById(R.id.detailSalaryNote);
        introText = findViewById(R.id.detailIntro);
        daysText = findViewById(R.id.detailDays);
        timeText = findViewById(R.id.detailTime);

        TutorInfo tutor = (TutorInfo) getIntent().getSerializableExtra("tutorInfo");

        if (tutor != null) {
            nameText.setText("👨‍🎓 姓名：" + tutor.getName());
            subjectsText.setText("📚 教授科目：" + listToString(tutor.getSubjects()));
            salaryText.setText("💰 薪資：" + tutor.getSalary() + " 元 / 小時");
            salaryNoteText.setText("📝 薪資備註：" + tutor.getSalaryNote());
            introText.setText("🧾 自我介紹：" + tutor.getIntro());
            daysText.setText("📅 可教學日：" + listToString(tutor.getAvailableDays()));
            timeText.setText("⏰ 時段：" + tutor.getStartTime() + " ～ " + tutor.getEndTime());
        }
    }
    private String listToString(List<String> list) {
        return list != null ? String.join("、", list) : "";
    }
}
