package com.softwaredesign.microbar.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.softwaredesign.microbar.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private Button btn_login;
    private TextView register;
    private EditText login_id;
    private EditText login_password;
    private String id;
    private String password;
    private String login_url = "http://119.29.178.68:8080/sysu-micro-bar/account/doLogin";
    //private String login_url = "http://xxx.tunnel.qydev.com/sysu-micro-bar/account/doLogin";

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = (Button) findViewById(R.id.btn_login);
        register = (TextView) findViewById(R.id.register);
        login_id = (EditText) findViewById(R.id.login_id);
        login_password = (EditText) findViewById(R.id.login_password);

        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        int accountId = sp.getInt("accountId", -1);

        if (accountId == -1) {
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = login_id.getText().toString();
                password = login_password.getText().toString();
                if (id.equals("")) {
                    Toast.makeText(LoginActivity.this, "请输入学号", Toast.LENGTH_SHORT).show();
                } else if (password.equals("")) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else {
                    check_and_login(id, password);
                }
            }
        });
    }

    public void check_and_login(final String id, final String password) {
        JSONObject jason = new JSONObject();
        try {
            jason.put("password", password);
            jason.put("stuNo", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpUtils.postString()
                .url(login_url)
                .content(jason.toString())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .connTimeOut(3000)
                .execute(new StringCallback() {

                    @Override
                    public void onResponse(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            boolean loginStatus = jsonObject.optBoolean("loginStatus");
                            int accountId = jsonObject.optInt("accountId");
                            String message = jsonObject.optString("message");
                            String headImageUrl = jsonObject.optString("headImageUrl");
                            String nickname = jsonObject.optString("nickname");
                            Log.d("LoginActivity", ""+accountId);
                            if (loginStatus) {
                                sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("accountId", accountId);
                                editor.putString("stuNo", id);
                                editor.putString("nickname", nickname);
                                editor.putString("headImageUrl", headImageUrl);
                                editor.putString("PASSWORD", password);
                                editor.apply();
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Request request, Exception e) {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

}