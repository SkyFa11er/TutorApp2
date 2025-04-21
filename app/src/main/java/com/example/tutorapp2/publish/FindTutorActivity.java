package com.example.tutorapp2.publish;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp2.R;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FindTutorActivity extends AppCompatActivity {

    private EditText editChildName, editPhone, editAddress, editSalary, editNote;
    private Spinner spinnerDistrict;
    private Button btnSelectSubjects, btnSelectDays, btnSubmit;
    private TextView selectedSubjectsText, selectedDaysText;

    private final String[] juniorSubjects = {
            "初中語文", "初中數學", "初中英語", "初中物理", "初中化學",
            "初中生物", "初中歷史", "初中地理", "初中政治"
    };
    private final String[] seniorSubjects = {
            "高中語文", "高中數學", "高中英語", "高中物理", "高中化學",
            "高中生物", "高中歷史", "高中地理", "高中政治"
    };
    private final String[] weekdays = {
            "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
    };
    private final String[] districts = {
            "天河區", "越秀區", "海珠區", "白雲區", "黃埔區", "番禺區", "荔灣區",
            "花都區", "南沙區", "從化區", "增城區"
    };

    private final List<String> selectedSubjects = new ArrayList<>();
    private final List<String> selectedDays = new ArrayList<>();
    private String selectedDistrict = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_tutor);

        editChildName = findViewById(R.id.editChildName);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editSalary = findViewById(R.id.editSalary);
        editNote = findViewById(R.id.editNote);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);

        btnSelectSubjects = findViewById(R.id.btnSelectSubjects);
        btnSelectDays = findViewById(R.id.btnSelectDays);
        btnSubmit = findViewById(R.id.btnSubmitFind);
        selectedSubjectsText = findViewById(R.id.selectedSubjectsText);
        selectedDaysText = findViewById(R.id.selectedDaysText);

        // 初始化地區 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistrict = districts[position];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSelectSubjects.setOnClickListener(v -> showSubjectDialog());
        btnSelectDays.setOnClickListener(v -> showDayDialog());
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
        builder.setTitle(title);
        builder.setMultiChoiceItems(subjectArray, checkedItems, (dialog, index, isChecked) -> {
            String subject = subjectArray[index];
            if (isChecked && !selectedSubjects.contains(subject)) {
                selectedSubjects.add(subject);
            } else if (!isChecked) {
                selectedSubjects.remove(subject);
            }
        });
        builder.setPositiveButton("確定", (dialog, which) ->
                selectedSubjectsText.setText(String.join("、", selectedSubjects)));
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDayDialog() {
        boolean[] checked = new boolean[weekdays.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇輔導時間");
        builder.setMultiChoiceItems(weekdays, checked, (dialog, which, isChecked) -> {
            String day = weekdays[which];
            if (isChecked && !selectedDays.contains(day)) {
                selectedDays.add(day);
            } else if (!isChecked) {
                selectedDays.remove(day);
            }
        });
        builder.setPositiveButton("確定", (dialog, which) ->
                selectedDaysText.setText(String.join("、", selectedDays)));
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void handleSubmit() {
        String childName = editChildName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String salaryStr = editSalary.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (childName.isEmpty() || phone.isEmpty() || selectedDistrict.isEmpty() ||
                selectedSubjects.isEmpty() || selectedDays.isEmpty() || salaryStr.isEmpty()) {
            Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("child_name", childName);
            json.put("phone", phone);
            json.put("district", selectedDistrict);
            json.put("address", address);
            json.put("salary", Integer.parseInt(salaryStr));
            json.put("subjects", String.join(",", selectedSubjects));
            json.put("days", String.join(",", selectedDays));
            json.put("note", note);

            new Thread(() -> {
                try {
                    URL url = new URL("http://8.138.229.36:3000/api/find-tutors");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    // 加入 token（需要是家長登入）
                    SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", MODE_PRIVATE);
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
                            Toast.makeText(this, "連線失敗", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "處理錯誤", Toast.LENGTH_SHORT).show();
        }
    }
}
