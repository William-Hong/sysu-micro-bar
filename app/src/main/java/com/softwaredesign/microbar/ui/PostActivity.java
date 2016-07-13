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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.model.Post;
import com.softwaredesign.microbar.util.GsonUtil;
import com.softwaredesign.microbar.util.ImageUtil;
import com.softwaredesign.microbar.util.SDCardUtil;
import com.softwaredesign.microbar.util.UploadUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

/**
 * Created by mac on 16/6/3.
 */
public class PostActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final String CREATEPOST = "createPost";

    private Toolbar postToolbar;
    private EditText postTitle;
    private Spinner postTagSpinner;
    private int postTag;
    private EditText postContent;
    // Key: uuid, Value: path
    private LinkedHashMap<String, Bitmap> pictures;

    private SharedPreferences sp;
    private int accountId;
    private ProgressDialog progressDialog;

    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();
        setListener();
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
                File file = createPhotoFile();
                Log.i("PostActivity", "photo's path is " + file);
                outputFileUri = Uri.fromFile(file);
                Log.i("PostActivity", "FileUri is " + outputFileUri);
                takePhoto(outputFileUri);
                break;
            case R.id.commit:
                uploadPost();
                Log.i("PostActivity", "" + postContent.getText());
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void init() {
        postToolbar = (Toolbar) findViewById(R.id.postToolbar);
        postToolbar.setTitle("发表帖子");
        postToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postTitle = (EditText)findViewById(R.id.postTitle);
        postTagSpinner = (Spinner) findViewById(R.id.postTag);
        postContent = (EditText) findViewById(R.id.postContent);
        pictures = new LinkedHashMap<>();
    }

    public void setListener() {
        final String[] postTags = getResources().getStringArray(R.array.postTags);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, postTags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postTagSpinner.setAdapter(adapter);
        postTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                postTag = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
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

    /**
     * 创建存储照片的文件
     *
     * @return 照片的存储路径(这里设置为公有的Pictures路径)
     */
    public static File createPhotoFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        try {
            return File.createTempFile(imageFileName, ".jpg", SDCardUtil.getFileDir(SDCardUtil.FILEDIR+"/"+SDCardUtil.FILEPHOTO));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拍照获取图片并保存在系统的Pictures路径下
     *
     * @param uri 拍照前产生的用来保存图片的Uri
     */
    public void takePhoto(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 上传帖子
     */
    public void uploadPost() {
        RequestParams params = new RequestParams();
        UploadUtil.addTitleAndTag(params, accountId, postTitle, postTag);
        UploadUtil.addContent(params, postContent, pictures);
        progressDialog = ProgressDialog.show(this, "上传帖子中","请稍候...",true);
        UploadUtil.sendMultipartRequest(CREATEPOST, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "上传失败,请检查网络", Toast.LENGTH_SHORT).show();
                Log.d("PostActivity", "" + statusCode);
                Log.d("PostActivity", responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                Post post = GsonUtil.parse(responseString, Post.class);
                Intent intent = new Intent();
                intent.putExtra("post", post);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 返回不成功时的处理
        if (resultCode != RESULT_OK) {
            Log.d("PostActivity", "canceled or other exception!");
            switch (requestCode) {
                case SELECT_PICTURE:
                    Log.i("PostActivity", "don't pick any picture");
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
                Log.i("PostActivity", "Uri: " + contentUri);
                insertImageIntoText(postContent, contentUri, pictures);
                break;

            case TAKE_PHOTO:
                insertImageIntoText(postContent, outputFileUri, pictures);
                // 发送Media Scanner更新通知
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outputFileUri));
                break;
            default:
                break;
        }
    }

    /**
     * 删掉拍照时产生的空文件(创建了文件却没有拍照导致文件为空)
     *
     * @param uri 拍照前创建的Uri
     * @return 是否删除成功
     */
    public boolean deleteEmptyPhotoPath(Uri uri) {
        String photoPath = getRealPathFromURI(uri);
        File f = new File(photoPath);
        Log.i("PostActivity", "photo path is " + photoPath);
        return f.exists() && f.delete();
    }

    /**
     * 将图片插入到文本中
     *
     * @param editText           文本对应的EditTex对象
     * @param contentUri         插入图片的Uri
     * @param spanStrings_pathes 存储图片的spanString和path
     */
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
        Log.i("PostActivity", id);
    }

    /**
     * 获取Uri对应的真实路径
     *
     * @param contentUri 需要获取真正路径的Uri
     * @return 图片的真实路径
     */
    public String getRealPathFromURI(Uri contentUri) {
        String path;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            Log.i("PostActivity", "cursor is null");
            path = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            path = cursor.getString(idx);
            cursor.close();
        }
        Log.i("PostActivity", "path: " + path);
        return path;
    }

    @Override
    protected void onDestroy() {
        for (Bitmap bitmap: pictures.values()) {
            bitmap.recycle();
        }
        pictures.clear();
        super.onDestroy();
    }
}