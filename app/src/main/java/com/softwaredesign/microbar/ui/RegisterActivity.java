package com.softwaredesign.microbar.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.model.registerUser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2016/5/27.
 */

public class RegisterActivity extends AppCompatActivity {
    private EditText id, account, pwd, pwd2, verification;
    private ImageView verificationImage;
    private Button registerBtn;
    private String ver = "";
    private ProgressDialog pd;
    private TextView toLogin;
    private SharedPreferences sp;

    private final String URL = "http://119.29.178.68:8080/sysu-micro-bar/account/doRegister";
    //private final String URL = "http://xxx.tunnel.qydev.com/sysu-micro-bar/account/doRegister";

    private static final char[] CHARS = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        id = (EditText) findViewById(R.id.register_id);
        account = (EditText) findViewById(R.id.register_account);
        pwd = (EditText) findViewById(R.id.register_pwd);
        pwd2 = (EditText) findViewById(R.id.register_pwd2);
        verification = (EditText) findViewById(R.id.register_Verification);
        verificationImage = (ImageView) findViewById(R.id.verificationImage);
        registerBtn = (Button) findViewById(R.id.registerButton);
        toLogin = (TextView) findViewById(R.id.registerToLogin);

        //生成随机字符串作为验证码，并申请验证码图
        new verifyImageGetter().execute();

        verificationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new verifyImageGetter().execute();
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId, userAccount, userPwd, userPwd2, verificationCode;
                userId = id.getText().toString();
                userAccount = account.getText().toString();
                userPwd = pwd.getText().toString();
                userPwd2 = pwd2.getText().toString();
                verificationCode = verification.getText().toString();

                Pattern p = Pattern.compile("[0-9]*");
                Matcher m = p.matcher(userId);

                if (ver.equals("")) {
                    Toast.makeText(RegisterActivity.this, "请等候验证码生成", Toast.LENGTH_LONG).show();
                } else if (userId.equals("") || userAccount.equals("")
                        || userPwd.equals("") || userPwd2.equals("")) {
                    Toast.makeText(RegisterActivity.this, "请输入完整信息", Toast.LENGTH_LONG).show();
                } else if (userId.length() != 8 || (!m.matches())) {
                    Toast.makeText(RegisterActivity.this, "请输入一个正确的学号", Toast.LENGTH_LONG).show();
                } else if (userAccount.length() > 15) {
                    Toast.makeText(RegisterActivity.this, "昵称长度过长，请修改", Toast.LENGTH_LONG).show();
                } else if (userPwd.length() > 15 || userPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "密码长度需为6到15位，请修改", Toast.LENGTH_LONG).show();
                } else if (!userPwd.equals(userPwd2)) {
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致，请重新输入",
                            Toast.LENGTH_LONG).show();
                } else if (!verificationCode.equalsIgnoreCase(ver)) {
                    Toast.makeText(RegisterActivity.this, "验证码错误，请重新输入",
                            Toast.LENGTH_LONG).show();
                } else {

                    pd = ProgressDialog.show(RegisterActivity.this, "", "正在注册");

                    OkHttpUtils
                            .postString()
                            .url(URL)
                            .content(new Gson().toJson(new registerUser(userAccount, userPwd, userId)))
                            .mediaType(MediaType.parse("application/json; charset=utf-8"))
                            .build()
                            .connTimeOut(20000)
                            .execute(new StringCallback() {

                                @Override
                                public void onError(Request request, Exception e) {
                                    pd.dismiss();
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivity.this, "连接失败，请重新注册", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(String s) {
                                    pd.dismiss();
                                    try {
                                        JSONObject jsonObject = new JSONObject(s);
                                        boolean registerStatus = jsonObject.optBoolean("registerStatus");
                                        int accountId = jsonObject.optInt("accountId");
                                        String message = jsonObject.optString("message");
                                        Log.d("RegisterActivity", "" + accountId);
                                        if (registerStatus) {
                                            sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putInt("accountId", accountId);
                                            editor.putString("stuNo", userId);
                                            editor.putString("nickname", userAccount);
                                            editor.putString("headImageUrl", "");
                                            editor.putString("PASSWORD", userPwd);
                                            editor.apply();
                                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });


                }
            }
        });
    }


    public String createCode() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            Random random = new Random();
            buffer.append(CHARS[random.nextInt(CHARS.length)]);//random为Random变量
        }
        return buffer.toString();
    }

    class verifyImageGetter extends AsyncTask<Void, Void, SoapPrimitive> {

        private final String NAMESPACE = "http://WebXml.com.cn/";
        private final String METHODNAME = "enValidateByte";
        private final String SOAPACTION = "http://WebXml.com.cn/enValidateByte";
        private final String URL = "http://webservice.webxml.com.cn/WebServices/ValidateCodeWebService.asmx";

        @Override
        protected void onPreExecute() {
            ver = createCode();
        }

        @Override
        protected SoapPrimitive doInBackground(Void... params) {
            SoapObject request = new SoapObject(NAMESPACE, METHODNAME);

            request.addProperty("byString", ver);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transportSE = new HttpTransportSE(URL);
            try {
                transportSE.call(SOAPACTION, envelope);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SoapObject result = (SoapObject) envelope.bodyIn;
            return (SoapPrimitive) result.getProperty("enValidateByteResult");
        }

        @Override
        protected void onPostExecute(SoapPrimitive soapPrimitive) {
            byte[] date = Base64.decode((soapPrimitive.toString()).getBytes(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(date, 0, date.length);
            verificationImage.setImageBitmap(bitmap);
        }
    }
}