package com.example.tutorapp2.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp2.MainActivity;
import com.example.tutorapp2.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "請輸入手機與密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            String loginJson = "{"
                    + "\"phone\":\"" + phone + "\","
                    + "\"password\":\"" + password + "\""
                    + "}";

            Log.d("LOGIN_DEBUG", "送出登入 JSON: " + loginJson);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(loginJson, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://8.138.229.36:3000/api/auth/login")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "連線失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resBody = response.body().string();
                    Log.d("LOGIN_DEBUG", "伺服器回應碼: " + response.code());
                    Log.d("LOGIN_DEBUG", "伺服器回應內容: " + resBody);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject obj = new JSONObject(resBody);
                            String token = obj.getString("token");
                            JSONObject user = obj.getJSONObject("user");
                            String role = user.optString("role", "unknown");
                            String name = user.optString("name", "訪客");

                            Log.d("LOGIN_DEBUG", "取得的 token: " + token);
                            Log.d("LOGIN_DEBUG", "使用者角色: " + role);
                            Log.d("LOGIN_DEBUG", "使用者名稱: " + name);

                            SharedPreferences prefs = getSharedPreferences("TutorAppPrefs", Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString("token", token)
                                    .putString("role", role)
                                    .putString("name", name)
                                    .apply();

                            Log.d("LOGIN_DEBUG", "token 和角色已成功儲存");

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Log.e("LOGIN_DEBUG", "JSON 解析錯誤: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "登入失敗：" + resBody, Toast.LENGTH_LONG).show());
                    }
                }
            });
        });
    }
}
