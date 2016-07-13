package com.softwaredesign.microbar.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.util.ImageUtil;
import com.softwaredesign.microbar.util.UploadUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

/**
 * Created by mac on 16/7/2.
 */
public class ReplyActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 0;
    private static final int TAKE_PHOTO = 1;

    private static final int MAKE_REPLY = 0;
    private static final int CREATE_FLOOR = 1;

    private static final String CREATEREPLY = "createReply";
    private static final String CREATEFLOOR = "createFloor";

    private Toolbar replyToolbar;
    private TextView typeString;
    private TextView replyWho;

    private EditText replyContent;
    // Key: uuid, Value: path
    private LinkedHashMap<String, Bitmap> pictures;

    private Uri outputFileUri;

    private ProgressDialog progressDialog;
    private SharedPreferences sp;
    private int accountId;

    private int type;
    private int postId;
    private String nickName;
    private int replyFloorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        Bundle bundle = getIntent().getExtras();
        type = bundle.getInt("type");
        if (type == MAKE_REPLY) {
            postId = bundle.getInt("postId");
            nickName = bundle.getString("nickName");
            replyFloorId = bundle.getInt("replyFloorId");
        } else if (type == CREATE_FLOOR) {
            postId = bundle.getInt("postId");
        }
        init();
        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        accountId = sp.getInt("accountId", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_picture:
                getImageFromGallery();
                break;
            case R.id.take_photo:
                File file = PostActivity.createPhotoFile();
                Log.i("ReplyActivity", "photo's path is " + file);
                outputFileUri = Uri.fromFile(file);
                Log.i("ReplyActivity", "FileUri is " + outputFileUri);
                takePhoto(outputFileUri);
                break;
            case R.id.commit:
                if (type == MAKE_REPLY) {
                    commitReply();
                    Log.i("ReplyActivity", "" + replyContent.getText());
                } else if (type == CREATE_FLOOR) {
                    createFloor();
                    Log.i("ReplyActivity", "" + replyContent.getText());
                }
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void init() {
        typeString = (TextView) findViewById(R.id.type);
        replyToolbar = (Toolbar) findViewById(R.id.postToolbar);
        replyToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        replyWho = (TextView) findViewById(R.id.replyWho);
        if (type == MAKE_REPLY) {
            typeString.setText("回复 ");
            replyToolbar.setTitle("编辑回复");
            replyWho.setText(nickName);
        } else if (type == CREATE_FLOOR) {
            typeString.setText("评论 ");
            replyToolbar.setTitle("编辑评论");
            replyWho.setVisibility(View.INVISIBLE);
        }
        setSupportActionBar(replyToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        replyContent = (EditText) findViewById(R.id.replyContent);
        pictures = new LinkedHashMap<>();
    }

    public void commitReply() {
        RequestParams params = new RequestParams();
        params.put("accountId", accountId);
        params.put("postId", postId);
        params.put("replyFloorId", replyFloorId);
        UploadUtil.addContent(params, replyContent, pictures);
        progressDialog = ProgressDialog.show(this, "创建回复中","请稍候...",true);
        UploadUtil.sendMultipartRequest(CREATEREPLY, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressDialog.dismiss();
                Toast.makeText(ReplyActivity.this, "上传失败,请检查网络", Toast.LENGTH_SHORT).show();
                Log.d("ReplyActivity", "" + statusCode);
                Log.d("ReplyActiviyu", responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                progressDialog.dismiss();
                Toast.makeText(ReplyActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                // setResult
                finish();
            }
        });
    }

    public void createFloor() {
        RequestParams params = new RequestParams();
        params.put("accountId", accountId);
        params.put("postId", postId);
        UploadUtil.addContent(params, replyContent, pictures);
        progressDialog = ProgressDialog.show(this, "发表评论中","请稍候...",true);
        UploadUtil.sendMultipartRequest(CREATEFLOOR, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressDialog.dismiss();
                Toast.makeText(ReplyActivity.this, "上传失败,请检查网络", Toast.LENGTH_SHORT).show();
                Log.d("ReplyActivity", "" + statusCode);
                Log.d("ReplyActivity", responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                progressDialog.dismiss();
                Toast.makeText(ReplyActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                // setResult
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 返回不成功时的处理
        if (resultCode != RESULT_OK) {
            Log.d("ReplyActivity", "canceled or other exception!");
            switch (requestCode) {
                case SELECT_PICTURE:
                    Log.i("ReplyActivity", "don't pick any picture");
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
                Log.i("ReplyActivity", "Uri: " + contentUri);
                insertImageIntoText(replyContent, contentUri, pictures);
                break;

            case TAKE_PHOTO:
                insertImageIntoText(replyContent, outputFileUri, pictures);
                // 发送Media Scanner更新通知
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outputFileUri));
                break;
            default:
                break;
        }
    }

    public void insertImageIntoText(EditText editText, Uri contentUri, Map<String, Bitmap> spanStrings_pathes) {
        // 获取真正的图片路径
        // Android4.4之后返回的URI只有图片编号,而不是真正的路径
        String path = getRealPathFromURI(contentUri);

        // 直接获取图片,对内存消耗太大,很容易OOM
        // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);

        // 压缩图片
        // 返回图片的宽度为EditText的宽度减去16或以上,
        // 否则在EditText显示一张图片时会出现两张相同的图片
        Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(path, editText.getWidth() - 16, editText.getWidth());
        Bitmap smallBitmap = ImageUtil.compressBitmap(bitmap);

        // 利用ImageSpan和SpannableString来显示图片
        String id = "[img=" + UUID.randomUUID() + "]";
        SpannableString spanString = new SpannableString(id);
        ImageSpan imageSpan = new ImageSpan(this, smallBitmap);
        spanString.setSpan(imageSpan, 0, id.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int index = editText.getSelectionStart();
        Editable editable = editText.getEditableText();

        if (index < 0 || index >= editable.length()) {
            editable.append(spanString);
        } else {
            editable.insert(index, spanString);
        }

        // 将图片的标识和路径存储为Map,用于上传到服务器
        spanStrings_pathes.put(id, smallBitmap);
        Log.i("ReplyActivity", id);
    }

    public boolean deleteEmptyPhotoPath(Uri uri) {
        String photoPath = getRealPathFromURI(uri);
        File f = new File(photoPath);
        Log.i("ReplyActivity", "photo path is " + photoPath);
        return f.exists() && f.delete();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String path;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            Log.i("ReplyActivity", "cursor is null");
            path = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            path = cursor.getString(idx);
            cursor.close();
        }
        Log.i("ReplyActivity", "path: " + path);
        return path;
    }
}
