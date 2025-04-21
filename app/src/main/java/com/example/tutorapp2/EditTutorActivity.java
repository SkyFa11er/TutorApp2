
package com.example.tutorapp2;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class EditTutorActivity extends AppCompatActivity {

    private TextView textName, edtSubjects, edtDays, textStartTime, textEndTime;
    private EditText edtSalary, edtSalaryNote, edtIntro;
    private Button btnSave;

    private final String[] allSubjects = {
            "初中語文", "初中數學", "初中英語", "初中物理", "初中化學",
            "高中語文", "高中數學", "高中英語", "高中物理", "高中化學"
    };
    private final String[] allDays = {
            "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
    };
    private boolean[] checkedSubjects, checkedDays;
    private List<String> selectedSubjects = new ArrayList<>();
    private List<String> selectedDays = new ArrayList<>();

    private int postId;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tutor);

        textName = findViewById(R.id.textName);
        edtSubjects = findViewById(R.id.edtSubjects);
        edtSalary = findViewById(R.id.edtSalary);
        edtSalaryNote = findViewById(R.id.edtSalaryNote);
        edtDays = findViewById(R.id.edtDays);
        textStartTime = findViewById(R.id.textStartTime);
        textEndTime = findViewById(R.id.textEndTime);
        edtIntro = findViewById(R.id.edtIntro);
        btnSave = findViewById(R.id.btnSave);

        postId = getIntent().getIntExtra("id", -1);
        name = getIntent().getStringExtra("name");
        String subjects = getIntent().getStringExtra("subjects");
        String salary = getIntent().getStringExtra("salary");
        String salaryNote = getIntent().getStringExtra("salary_note");
        String intro = getIntent().getStringExtra("intro");
        String days = getIntent().getStringExtra("available_days");
        String startTime = getIntent().getStringExtra("start_time");
        String endTime = getIntent().getStringExtra("end_time");

        textName.setText("🎓 " + name);
        edtSubjects.setText(subjects);
        edtSalary.setText(salary);
        edtSalaryNote.setText(salaryNote);
        edtIntro.setText(intro);
        edtDays.setText(days);
        textStartTime.setText(startTime);
        textEndTime.setText(endTime);

        selectedSubjects = new ArrayList<>(Arrays.asList(subjects.split(",")));
        selectedDays = new ArrayList<>(Arrays.asList(days.split(",")));

        checkedSubjects = new boolean[allSubjects.length];
        for (int i = 0; i < allSubjects.length; i++) {
            checkedSubjects[i] = selectedSubjects.contains(allSubjects[i]);
        }

        checkedDays = new boolean[allDays.length];
        for (int i = 0; i < allDays.length; i++) {
            checkedDays[i] = selectedDays.contains(allDays[i]);
        }

        edtSubjects.setOnClickListener(v -> showSubjectsDialog());
        edtDays.setOnClickListener(v -> showDaysDialog());
        textStartTime.setOnClickListener(v -> showTimePicker(textStartTime));
        textEndTime.setOnClickListener(v -> showTimePicker(textEndTime));
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void showSubjectsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("選擇教授科目")
                .setMultiChoiceItems(allSubjects, checkedSubjects, (dialog, which, isChecked) -> {
                    if (isChecked) selectedSubjects.add(allSubjects[which]);
                    else selectedSubjects.remove(allSubjects[which]);
                })
                .setPositiveButton("確定", (dialog, which) -> edtSubjects.setText(TextUtils.join(",", selectedSubjects)))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDaysDialog() {
        new AlertDialog.Builder(this)
                .setTitle("選擇可教日期")
                .setMultiChoiceItems(allDays, checkedDays, (dialog, which, isChecked) -> {
                    if (isChecked) selectedDays.add(allDays[which]);
                    else selectedDays.remove(allDays[which]);
                })
                .setPositiveButton("確定", (dialog, which) -> edtDays.setText(TextUtils.join(",", selectedDays)))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showTimePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, h, m) -> target.setText(String.format("%02d:%02d", h, m)), hour, minute, true).show();
    }

    private void saveChanges() {
        String salaryStr = edtSalary.getText().toString().trim();
        String salaryNote = edtSalaryNote.getText().toString().trim();
        String intro = edtIntro.getText().toString().trim();
        String startTime = textStartTime.getText().toString().trim();
        String endTime = textEndTime.getText().toString().trim();
        String subjects = TextUtils.join(",", selectedSubjects);
        String days = TextUtils.join(",", selectedDays);

        if (!startTime.endsWith(":00")) startTime += ":00";
        if (!endTime.endsWith(":00")) endTime += ":00";

        if (subjects.isEmpty() || salaryStr.isEmpty() || days.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
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
            json.put("subjects", subjects);
            json.put("salary", salary);
            json.put("salary_note", salaryNote);
            json.put("intro", intro);
            json.put("available_days", days);
            json.put("start_time", startTime);
            json.put("end_time", endTime);
            Log.d("EDIT_JSON", "送出 JSON: " + json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "建立 JSON 錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String url = "http://8.138.229.36:3000/api/tutors/" + postId;

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditTutorActivity.this, "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d("EDIT_JSON", "伺服器回應: " + response.code() + ", " + resBody);
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditTutorActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditTutorActivity.this, "修改失敗：" + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
