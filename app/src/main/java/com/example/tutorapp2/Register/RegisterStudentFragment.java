package com.example.tutorapp2.Register;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tutorapp2.MainActivity;
import com.example.tutorapp2.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class RegisterStudentFragment extends Fragment {

    private EditText edtName, edtPhone, edtPassword, edtSchool, edtMajor;
    private Spinner spinnerYear;
    private Button btnRegister;

    public RegisterStudentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_student, container, false);

        edtName = view.findViewById(R.id.edtName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtSchool = view.findViewById(R.id.edtSchool);
        edtMajor = view.findViewById(R.id.edtMajor);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        btnRegister = view.findViewById(R.id.btnRegister);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"2020", "2021", "2022", "2023", "2024", "2025"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String school = edtSchool.getText().toString().trim();
            String major = edtMajor.getText().toString().trim();
            String enrollYear = spinnerYear.getSelectedItem().toString();

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()
                    || school.isEmpty() || major.isEmpty()) {
                Toast.makeText(getContext(), "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                return;
            }

            String jsonBody = "{"
                    + "\"name\":\"" + name + "\","
                    + "\"phone\":\"" + phone + "\","
                    + "\"password\":\"" + password + "\","
                    + "\"school\":\"" + school + "\","
                    + "\"major\":\"" + major + "\","
                    + "\"enroll_year\":\"" + enrollYear + "\""
                    + "}";

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://8.138.229.36:3000/api/users/register/student")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resText = response.body().string();
                    if (response.isSuccessful()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "註冊成功，登入中...", Toast.LENGTH_SHORT).show());

                        loginAfterRegister(phone, password); // ✅ 呼叫登入
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "註冊失敗：" + resText, Toast.LENGTH_LONG).show());
                    }
                }
            });
        });

        return view;
    }

    private void loginAfterRegister(String phone, String password) {
        OkHttpClient client = new OkHttpClient();
        String loginJson = "{"
                + "\"phone\":\"" + phone + "\","
                + "\"password\":\"" + password + "\""
                + "}";

        RequestBody body = RequestBody.create(loginJson, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://8.138.229.36:3000/api/auth/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "自動登入失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resBody = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(resBody);
                        String token = obj.getString("token");

                        // 儲存 token 與身份
                        SharedPreferences prefs = requireActivity().getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("token", token).apply();
                        prefs.edit().putString("role", "student").apply();

                        // 跳轉至主頁
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "自動登入失敗（帳密錯誤？）", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
