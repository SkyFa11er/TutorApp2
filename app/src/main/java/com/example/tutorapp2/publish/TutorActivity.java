package com.example.tutorapp2.publish;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import com.example.tutorapp2.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TutorActivity extends AppCompatActivity {

    private Button btnSelectSubjects, btnSelectDays, btnStartTime, btnEndTime, btnSubmit;
    private EditText editSalary, editSalaryNote, editIntro;
    private TextView selectedSubjectsText, selectedDaysText, startTimeText, endTimeText;

    private final List<String> subjectList = new ArrayList<>();
    private final List<String> dayList = new ArrayList<>();

    private String startTime = "", endTime = "";

    private final String[] juniorSubjects = {
            "初中語文", "初中數學", "初中英語", "初中物理", "初中化學",
            "初中生物", "初中歷史", "初中地理", "初中政治"
    };
    private final String[] seniorSubjects = {
            "高中語文", "高中數學", "高中英語", "高中物理", "高中化學",
            "高中生物", "高中歷史", "高中地理", "高中政治"
    };
    private final String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor);

        // 綁定元件
        btnSelectSubjects = findViewById(R.id.btnSelectSubjects);
        btnSelectDays = findViewById(R.id.btnSelectDays);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnSubmit = findViewById(R.id.btnSubmit);

        editSalary = findViewById(R.id.editSalary);
        editSalaryNote = findViewById(R.id.editSalaryNote);
        editIntro = findViewById(R.id.editIntro);

        selectedSubjectsText = findViewById(R.id.selectedSubjectsText);
        selectedDaysText = findViewById(R.id.selectedDaysText);
        startTimeText = findViewById(R.id.startTimeText);
        endTimeText = findViewById(R.id.endTimeText);

        // 綁定點擊事件
        btnSelectSubjects.setOnClickListener(v -> showSubjectDialog());
        btnSelectDays.setOnClickListener(v -> showDayDialog());
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void showSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇科目分類");

        String[] types = {"初中科目", "高中科目"};
        builder.setItems(types, (dialog, which) -> {
            if (which == 0) {
                showMultiSelectDialog("初中科目", juniorSubjects);
            } else {
                showMultiSelectDialog("高中科目", seniorSubjects);
            }
        });

        builder.show();
    }

    private void showMultiSelectDialog(String title, String[] subjectArray) {
        boolean[] checkedItems = new boolean[subjectArray.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇 " + title);
        builder.setMultiChoiceItems(subjectArray, checkedItems, (dialog, index, isChecked) -> {
            String subject = subjectArray[index];
            if (isChecked && !subjectList.contains(subject)) {
                subjectList.add(subject);
            } else if (!isChecked) {
                subjectList.remove(subject);
            }
        });

        builder.setPositiveButton("確定", (dialog, which) ->
                selectedSubjectsText.setText(String.join("、", subjectList))
        );
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDayDialog() {
        boolean[] checked = new boolean[weekdays.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇可教學日");
        builder.setMultiChoiceItems(weekdays, checked, (dialog, which, isChecked) -> {
            String day = weekdays[which];
            if (isChecked && !dayList.contains(day)) {
                dayList.add(day);
            } else if (!isChecked) {
                dayList.remove(day);
            }
        });
        builder.setPositiveButton("確定", (dialog, which) ->
                selectedDaysText.setText(String.join("、", dayList)));
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (isStart) {
                startTime = time;
                startTimeText.setText("開始：" + time);
            } else {
                endTime = time;
                endTimeText.setText("結束：" + time);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void handleSubmit() {
        String salary = editSalary.getText().toString().trim();
        String salaryNote = editSalaryNote.getText().toString().trim();
        String intro = editIntro.getText().toString().trim();

        if (subjectList.isEmpty() || dayList.isEmpty() || salary.isEmpty() || intro.isEmpty()
                || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("subjects", new JSONArray(subjectList));
            json.put("salary", salary);
            json.put("salary_note", salaryNote);
            json.put("intro", intro);
            json.put("available_days", new JSONArray(dayList));
            json.put("start_time", startTime);
            json.put("end_time", endTime);

            new Thread(() -> {
                try {
                    URL url = new URL("http://8.138.229.36:3000/api/tutors");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    SharedPreferences prefs = TutorActivity.this.getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
                    String token = prefs.getString("token", "");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes());
                    os.flush();
                    os.close();

                    int code = conn.getResponseCode();
                    runOnUiThread(() -> {
                        if (code == 200 || code == 201) {
                            Toast.makeText(this, "發布成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "發布失敗（代碼：" + code + "）", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "連線錯誤", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "處理失敗", Toast.LENGTH_SHORT).show();
        }
    }
}
