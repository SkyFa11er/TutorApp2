
package com.example.tutorapp2.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp2.R;
import com.example.tutorapp2.adapter.TutorAdapter;
import com.example.tutorapp2.adapter.FindTutorAdapter;
import com.example.tutorapp2.model.TutorInfo;
import com.example.tutorapp2.model.FindTutorInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import okhttp3.*;

public class HomeFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton radioDo, radioFind;
    private RecyclerView recyclerView;

    private TutorAdapter tutorAdapter;
    private FindTutorAdapter findTutorAdapter;

    private List<TutorInfo> tutorList = new ArrayList<>();
    private List<FindTutorInfo> findList = new ArrayList<>();

    private OkHttpClient client = new OkHttpClient();
    private static final String TAG = "HOME_FRAGMENT";
    private boolean isDoingTutor = true;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        radioGroup = view.findViewById(R.id.radioGroup);
        radioDo = view.findViewById(R.id.radioDo);
        radioFind = view.findViewById(R.id.radioFind);
        recyclerView = view.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnGoToTutor = view.findViewById(R.id.btnGoTutor);
        Button btnGoFindTutor = view.findViewById(R.id.btnGoFindTutor);

// 判斷角色：顯示對應按鈕
        SharedPreferences prefs = requireContext().getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "");

        if ("student".equals(role)) {
            btnGoToTutor.setVisibility(View.VISIBLE);
            btnGoFindTutor.setVisibility(View.GONE);
        } else if ("parent".equals(role)) {
            btnGoToTutor.setVisibility(View.GONE);
            btnGoFindTutor.setVisibility(View.VISIBLE);
        } else {
            btnGoToTutor.setVisibility(View.GONE);
            btnGoFindTutor.setVisibility(View.GONE);
        }

// 做家教
        btnGoToTutor.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.tutorapp2.publish.TutorActivity.class);
            startActivity(intent);
        });

// 招家教
        btnGoFindTutor.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.tutorapp2.publish.FindTutorActivity.class);
            startActivity(intent);
        });


        // 預設選擇做家教
        radioDo.setChecked(true);
        loadDoTutorData();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDo) {
                isDoingTutor = true;
                loadDoTutorData();
            } else if (checkedId == R.id.radioFind) {
                isDoingTutor = false;
                loadFindTutorData();
            }
        });


        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            setHasOptionsMenu(true);
        }
    }

    private void loadDoTutorData() {
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/tutors")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "讀取做家教資料失敗", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d(TAG, "做家教資料: " + resBody);

                try {
                    JSONArray array = new JSONArray(resBody);
                    tutorList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int id = obj.getInt("id");
                        String name = obj.optString("name", "學生");
                        List<String> subjects = Arrays.asList(obj.optString("subjects", "").split(","));
                        String salary = obj.optString("salary", "");
                        String salaryNote = obj.optString("salary_note", "");
                        String intro = obj.optString("intro", "");
                        List<String> days = Arrays.asList(obj.optString("available_days", "").split(","));
                        String startTime = obj.optString("start_time", "");
                        String endTime = obj.optString("end_time", "");

                        tutorList.add(new TutorInfo(id, name, subjects, salary, salaryNote, intro, days, startTime, endTime));
                    }

                    requireActivity().runOnUiThread(() -> {
                        tutorAdapter = new TutorAdapter(tutorList, null, false);
                        recyclerView.setAdapter(tutorAdapter);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadFindTutorData() {
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/find-tutors")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "讀取招家教資料失敗", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String resBody = response.body().string();
                Log.d(TAG, "招家教資料: " + resBody);

                try {
                    JSONArray array = new JSONArray(resBody);
                    findList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int id = obj.getInt("id");
                        String name = obj.optString("child_name", "學員");
                        String subjects = obj.optString("subjects", "");
                        int salary = obj.optInt("salary", 0);
                        String days = obj.optString("days", "");
                        String note = obj.optString("note", "");
                        String district = obj.optString("district", "");
                        Log.d("DEBUG_DISTRICT", "API 解析地區：" + district);
                        findList.add(new FindTutorInfo(id, name, subjects, salary, days, note, "", district, ""));

                    }

                    requireActivity().runOnUiThread(() -> {
                        FindTutorAdapter adapter = new FindTutorAdapter(findList, null, false); // 第三個參數設為 false 就不顯示按鈕
                        recyclerView.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }




    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("進階篩選");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        // 分類式科目選擇
        String[] juniorSubjects = {"初中語文", "初中數學", "初中英語", "初中物化", "初中生物", "初中歷史", "初中地理", "初中政治"};
        String[] seniorSubjects = {"高中語文", "高中數學", "高中英語", "高中物化", "高中生物", "高中歷史", "高中地理", "高中政治"};
        List<String> selectedSubjects = new ArrayList<>();

        Button btnChooseSubjects = dialogView.findViewById(R.id.btnChooseSubjects);
        TextView txtChosenSubjects = dialogView.findViewById(R.id.txtChosenSubjects);
        btnChooseSubjects.setOnClickListener(v -> {
            AlertDialog.Builder categoryDialog = new AlertDialog.Builder(getContext());
            categoryDialog.setTitle("選擇科目分類");
            categoryDialog.setItems(new String[]{"初中科目", "高中科目"}, (dialog, which) -> {
                String[] subjects = (which == 0) ? juniorSubjects : seniorSubjects;
                boolean[] checked = new boolean[subjects.length];

                AlertDialog.Builder subDialog = new AlertDialog.Builder(getContext());
                subDialog.setTitle("選擇科目（可多選）");
                subDialog.setMultiChoiceItems(subjects, checked, (d, index, isChecked) -> {
                    String subject = subjects[index];
                    if (isChecked && !selectedSubjects.contains(subject)) {
                        selectedSubjects.add(subject);
                    } else if (!isChecked) {
                        selectedSubjects.remove(subject);
                    }
                });
                subDialog.setPositiveButton("確定", (d, w) -> txtChosenSubjects.setText(String.join(", ", selectedSubjects)));
                subDialog.setNegativeButton("取消", null);
                subDialog.show();
            });
            categoryDialog.show();
        });

        Spinner spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        if (isDoingTutor) {
            spinnerDistrict.setVisibility(View.GONE);
            dialogView.findViewById(R.id.labelDistrict).setVisibility(View.GONE);
        } else {
            String[] districts = {"天河區", "越秀區", "海珠區", "白雲區", "黃埔區","番禺區", "荔灣區", "花都區", "南沙區", "增城區", "從化區"};
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, districts);
            spinnerDistrict.setAdapter(districtAdapter);
        }

        EditText edtMinSalary = dialogView.findViewById(R.id.edtMinSalary);

        String[] allDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        boolean[] checkedDays = new boolean[allDays.length];
        List<String> selectedDays = new ArrayList<>();

        Button btnChooseDays = dialogView.findViewById(R.id.btnChooseDays);
        TextView txtChosenDays = dialogView.findViewById(R.id.txtChosenDays);
        btnChooseDays.setOnClickListener(v -> {
            AlertDialog.Builder dayDialog = new AlertDialog.Builder(getContext());
            dayDialog.setTitle("選擇可教日期");
            dayDialog.setMultiChoiceItems(allDays, checkedDays, (dialog, which, isChecked) -> {
                checkedDays[which] = isChecked;
            });
            dayDialog.setPositiveButton("確定", (dialog, which) -> {
                selectedDays.clear();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < allDays.length; i++) {
                    if (checkedDays[i]) {
                        selectedDays.add(allDays[i]);
                        sb.append(allDays[i]).append(", ");
                    }
                }
                if (sb.length() > 0) sb.setLength(sb.length() - 2);
                txtChosenDays.setText(sb.toString());
            });
            dayDialog.setNegativeButton("取消", null);
            dayDialog.show();
        });

        builder.setPositiveButton("篩選", (dialog, which) -> {
            String subjectParam = String.join(",", selectedSubjects);
            String dayParam = String.join(",", selectedDays);
            String minSalary = edtMinSalary.getText().toString().trim();
            String district = spinnerDistrict.getSelectedItem() != null ? spinnerDistrict.getSelectedItem().toString() : "";

            String url = "http://8.138.229.36:3000/api/tutors/filter?type=" +
                    (isDoingTutor ? "do" : "find") +
                    "&subjects=" + subjectParam +
                    "&minSalary=" + minSalary +
                    "&days=" + dayParam;

            if (!isDoingTutor && !district.isEmpty()) {
                url += "&district=" + district;
            }

            Log.d("FILTER", "呼叫篩選 API：" + url);

            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "查詢失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resBody = response.body().string();
                    Log.d("FILTER", "API回應：" + resBody);

                    try {
                        JSONArray array = new JSONArray(resBody);

                        requireActivity().runOnUiThread(() -> {
                            try {
                                if (isDoingTutor) {
                                    tutorList.clear();
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        int id = obj.getInt("id");
                                        String name = obj.optString("name", "學生");
                                        List<String> subjects = Arrays.asList(obj.optString("subjects", "").split(","));
                                        String salary = obj.optString("salary", "");
                                        String salaryNote = obj.optString("salary_note", "");
                                        String intro = obj.optString("intro", "");
                                        List<String> days = Arrays.asList(obj.optString("available_days", "").split(","));
                                        String startTime = obj.optString("start_time", "");
                                        String endTime = obj.optString("end_time", "");

                                        tutorList.add(new TutorInfo(id, name, subjects, salary, salaryNote, intro, days, startTime, endTime));
                                    }
                                    tutorAdapter = new TutorAdapter(tutorList, null, false);
                                    recyclerView.setAdapter(tutorAdapter);

                                } else {
                                    findList.clear();
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        int id = obj.getInt("id");
                                        String name = obj.optString("child_name", "學員");
                                        String subjects = obj.optString("subjects", "");
                                        int salary = obj.optInt("salary", 0);
                                        String days = obj.optString("days", "");
                                        String note = obj.optString("note", "");

                                        findList.add(new FindTutorInfo(id, name, subjects, salary, days, note));
                                    }
                                    findTutorAdapter = new FindTutorAdapter(findList, null, false);
                                    recyclerView.setAdapter(findTutorAdapter);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "資料解析錯誤", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "解析錯誤", Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }

            });
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog(); // 彈出篩選對話框
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
