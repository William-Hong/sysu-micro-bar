package com.softwaredesign.microbar.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.util.ImageUtil;
import com.softwaredesign.microbar.util.PostUtil;
import com.softwaredesign.microbar.util.SDCardUtil;
import com.softwaredesign.microbar.util.UploadUtil;
import com.squareup.okhttp.Request;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by mac on 16/7/5.
 */
public class EditAccountFragment extends Fragment {
    private static final int SELECT_PICTURE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private static final String UPLOADHEADIMAGE = "uploadHeadImage";
    private static final String MODIFYPERSONALINFO = "modifyPersonalInfo";

    private FragmentCallback fragmentCallback;
    private FragmentActivity fragmentActivity;
    private ImageView userNewPortrait;
    private ImageButton addFromGallery;
    private ImageButton addFromCamera;
    private EditText userNewNickname;
    private EditText userNewPassword;
    private Button saveAccount;

    private Bitmap upload_portrait;
    private int accountId;
    private String oldPortraitUrl;
    private String oldNickname;
    private String oldPassword;

    private Uri outputFileUri;
    private SharedPreferences sp;

    public static EditAccountFragment getEditAccountFragment() {
        EditAccountFragment editAccountFragment = new EditAccountFragment();
        return editAccountFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentCallback = (FragmentCallback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_edit, container, false);
        init(view);
        addListener();
        return view;
    }

    public void init(View v) {
        fragmentActivity = getActivity();
        addFromGallery = (ImageButton) v.findViewById(R.id.addFromGallery);
        addFromCamera = (ImageButton) v.findViewById(R.id.addFromCamera);
        userNewPortrait = (ImageView) v.findViewById(R.id.userNewPortrait);
        userNewNickname = (EditText) v.findViewById(R.id.userNewNickname);
        userNewPassword = (EditText) v.findViewById(R.id.userNewPassword);
        saveAccount = (Button) v.findViewById(R.id.saveAccount);
        sp = fragmentActivity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        accountId = sp.getInt("accountId", 0);
        oldNickname = sp.getString("nickname", "");
        oldPassword = sp.getString("PASSWORD", "");
        oldPortraitUrl = sp.getString("headImageUrl", "");
        String path = SDCardUtil.getSdPath()+SDCardUtil.FILEDIR+"/"+SDCardUtil.CACHE+"/"+"user_portrait_"+accountId+".jpg";
        File file = new File(path);
        // 先从本地路径读取头像
        if (file.exists()) {
            Picasso.with(fragmentActivity)
                    .load(file)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resizeDimen(R.dimen.set_portrait_width, R.dimen.set_portrait_height)
                    .centerInside()
                    .into(userNewPortrait);
        } else if (!oldPortraitUrl.isEmpty()) {
            Picasso.with(fragmentActivity)
                    .load(oldPortraitUrl)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.default_portrait)
                    .error(R.drawable.default_portrait)
                    .resizeDimen(R.dimen.set_portrait_width, R.dimen.set_portrait_height)
                    .centerInside()
                    .into(userNewPortrait);
        }
        userNewNickname.setText(oldNickname);
        userNewPassword.setText(oldPassword);
    }

    public void addListener() {
        addFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });
        addFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = PostActivity.createPhotoFile();
                outputFileUri = Uri.fromFile(file);
                takePhoto(outputFileUri);
            }
        });
        saveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newNickname = userNewNickname.getText().toString();
                final String newPassword = userNewPassword.getText().toString();
                // 上传的同时将图片保存在本地
                if (upload_portrait != null) {
                    RequestParams params = new RequestParams();
                    UploadUtil.uploadHeadImage(params, accountId, ImageUtil.storeUserPortrait(upload_portrait, accountId));
                    UploadUtil.sendRequest(UPLOADHEADIMAGE, params, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.d("EditAccountFragment", "" + statusCode);
                            Log.d("EditAccountFragment", responseString);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseString);
                                boolean status = jsonObject.optBoolean("status");
                                String headImageUrl = jsonObject.optString("headImageUrl");
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("headImageUrl", headImageUrl);
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("EditAccountFragment", responseString);
                        }
                    });
                }

                PostUtil.modifyPersonalInfo(MODIFYPERSONALINFO,
                        accountId,
                        newNickname,
                        newPassword,
                        new StringCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response) {
                                Log.d("EditAccountFragment", response);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("nickname", newNickname);
                                editor.putString("PASSWORD", newPassword);
                                editor.apply();
                                Toast.makeText(fragmentActivity, "修改成功", Toast.LENGTH_SHORT).show();
                                fragmentCallback.updateNavHeader();
                            }
                        });
                fragmentCallback.updateNavHeader();
            }
        });
    }

    // 从系统相册中获取图片
    public void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
    }

    public void takePhoto(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    public boolean deleteEmptyPhotoPath(Uri uri) {
        String photoPath = getRealPathFromURI(uri);
        File f = new File(photoPath);
        Log.i("PostActivity", "photo path is " + photoPath);
        return f.exists() && f.delete();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 返回不成功时的处理
        if (resultCode != FragmentActivity.RESULT_OK) {
            Log.d("EditAccountFragment", "canceled or other exception!");
            switch (requestCode) {
                case SELECT_PICTURE:
                    Log.i("EditAccountFragment", "don't pick any picture");
                    break;
                case TAKE_PHOTO:
                    deleteEmptyPhotoPath(outputFileUri);
                    break;
                default:
                    break;
            }
            return;
        }

        switch (requestCode) {
            case SELECT_PICTURE:
                Uri contentUri = data.getData();
                Log.i("EditAccountFragment", "Uri: " + contentUri);
                doCrop(contentUri);
                break;

            case TAKE_PHOTO:
                // 发送Media Scanner更新通知
                fragmentActivity.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outputFileUri));
                Log.d("EditAccountFragment", getRealPathFromURI(outputFileUri));
                doCrop(outputFileUri);
                break;

            case CROP_FROM_CAMERA:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    upload_portrait = bundle.getParcelable("data");
                    userNewPortrait.setImageBitmap(upload_portrait);
                }
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void doCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("outputX", 120);
        intent.putExtra("outputY", 120);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_FROM_CAMERA);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String path;
        Cursor cursor = fragmentActivity.getApplicationContext().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            Log.i("EditAccountFragment", "cursor is null");
            path = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            path = cursor.getString(idx);
            cursor.close();
        }
        Log.i("EditAccountFragment", "path: " + path);
        return path;
    }

    public interface FragmentCallback {
        void updateNavHeader();
    }
}
