package com.example.tutorapp2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class EditFindTutorActivity extends AppCompatActivity {

    private TextView edtSubjects, edtDays;
    private EditText edtSalary, edtNote;
    private Button btnSave;
    private int postId;
    private String childName, phone, district, address;

    private final String[] juniorSubjects = {"初中語文", "初中數學", "初中英語", "初中物理", "初中化學", "初中生物", "初中歷史", "初中地理", "初中政治"};
    private final String[] seniorSubjects = {"高中語文", "高中數學", "高中英語", "高中物理", "高中化學", "高中生物", "高中歷史", "高中地理", "高中政治"};
    private final String[] allDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    private final List<String> selectedSubjects = new ArrayList<>();
    private final List<String> selectedDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_find_tutor);

        edtSubjects = findViewById(R.id.edtSubjects);
        edtSalary = findViewById(R.id.edtSalary);
        edtDays = findViewById(R.id.edtDays);
        edtNote = findViewById(R.id.edtNote);
        btnSave = findViewById(R.id.btnSave);

        Intent intent = getIntent();
        postId = intent.getIntExtra("id", -1);
        childName = intent.getStringExtra("child_name");
        phone = intent.getStringExtra("phone");
        district = intent.getStringExtra("district");
        address = intent.getStringExtra("address");

        ((TextView) findViewById(R.id.textChildName)).setText("\uD83D\uDC67 " + childName);

        // 初始值設置
        String[] preSubjects = intent.getStringExtra("subjects").split(",");
        for (String s : preSubjects) selectedSubjects.add(s);
        edtSubjects.setText(String.join(",", selectedSubjects));

        int salary = intent.getIntExtra("salary", 0);
        edtSalary.setText(String.valueOf(salary));

        String[] preDays = intent.getStringExtra("days").split(",");
        for (String d : preDays) selectedDays.add(d);
        edtDays.setText(String.join(",", selectedDays));

        edtNote.setText(intent.getStringExtra("note"));

        edtSubjects.setOnClickListener(v -> showSubjectCategoryDialog());
        edtDays.setOnClickListener(v -> showDayMultiSelectDialog());

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void showSubjectCategoryDialog() {
        String[] categories = {"初中", "高中"};
        new AlertDialog.Builder(this)
                .setTitle("選擇科目分類")
                .setItems(categories, (dialog, which) -> {
                    if (which == 0) {
                        showSubjectMultiSelectDialog(juniorSubjects);
                    } else {
                        showSubjectMultiSelectDialog(seniorSubjects);
                    }
                }).show();
    }

    private void showSubjectMultiSelectDialog(String[] subjects) {
        boolean[] checked = new boolean[subjects.length];
        for (int i = 0; i < subjects.length; i++) {
            checked[i] = selectedSubjects.contains(subjects[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("選擇科目（可多選）")
                .setMultiChoiceItems(subjects, checked, (dialog, which, isChecked) -> {
                    if (isChecked) selectedSubjects.add(subjects[which]);
                    else selectedSubjects.remove(subjects[which]);
                })
                .setPositiveButton("確定", (dialog, which) -> edtSubjects.setText(String.join(",", selectedSubjects)))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDayMultiSelectDialog() {
        boolean[] checked = new boolean[allDays.length];
        for (int i = 0; i < allDays.length; i++) {
            checked[i] = selectedDays.contains(allDays[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("選擇可輔導時間（可多選）")
                .setMultiChoiceItems(allDays, checked, (dialog, which, isChecked) -> {
                    if (isChecked) selectedDays.add(allDays[which]);
                    else selectedDays.remove(allDays[which]);
                })
                .setPositiveButton("確定", (dialog, which) -> edtDays.setText(String.join(",", selectedDays)))
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveChanges() {
        String subjects = String.join(",", selectedSubjects);
        String days = String.join(",", selectedDays);
        String salaryStr = edtSalary.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (subjects.isEmpty() || salaryStr.isEmpty() || days.isEmpty() ||
                childName == null || phone == null || district == null || address == null ||
                childName.trim().isEmpty() || phone.trim().isEmpty() ||
                district.trim().isEmpty() || address.trim().isEmpty()) {
            Toast.makeText(this, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show();
            return;
        }

        int salary;
        try {
            salary = Integer.parseInt(salaryStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "薪資格式錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("child_name", childName);
            json.put("phone", phone);
            json.put("district", district);
            json.put("address", address);
            json.put("subjects", subjects);
            json.put("salary", salary);
            json.put("days", days);
            json.put("note", note);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "資料建立錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("EDIT_JSON", "送出的內容: " + json);

        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String url = "http://8.138.229.36:3000/api/find-tutors/" + postId;

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(EditFindTutorActivity.this, "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d("EDIT_JSON", "伺服器回應：" + response.code() + ", 內容：" + resBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditFindTutorActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditFindTutorActivity.this, "修改失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}