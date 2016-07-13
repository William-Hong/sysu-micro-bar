package com.softwaredesign.microbar.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mac on 16/6/4.
 */
public class UploadUtil {
    private static final String BASE_URL = "http://119.29.178.68:8080/sysu-micro-bar/";
    //private static final String BASE_URL = "http://xxx.tunnel.qydev.com/sysu-micro-bar/";
    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.addHeader("Accept", "application/json");
    }
    /**
     *
     * @param params 上传参数
     * @param accountId 账户ID
     * @param postTitle 帖子标题
     * @param postTag 帖子标签
     * @return 上传参数
     */
    public static RequestParams addTitleAndTag(RequestParams params, int accountId, EditText postTitle, int postTag) {
        params.put("accountId", accountId);
        params.put("title", postTitle.getText());
        params.put("tag", postTag);
        return params;
    }

    public static RequestParams uploadHeadImage(RequestParams params, int accountId, File file) {
        params.put("accountId", accountId);
        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     *
     * @param params 上传参数
     * @param content 帖子内容
     * @param spanStrings_pathes
     * @return 上传参数
     */
    public static RequestParams addContent(RequestParams params, EditText content, Map<String, Bitmap> spanStrings_pathes) {
        String detail = content.getText().toString();
        params.put("detail", detail);
        Log.d("UploadUtil", detail);
        // 利用正则表达式在文本中去匹配表示图片的key,得到图片的真实路径
        // 注意map中的键值对数目与实际的detail中的img数目可能不同(因为用户插入图片后又删除了该图片)
        // 所以需要重新计算实际需要上传的图片数目
        Iterator<Map.Entry<String, Bitmap>> iter = spanStrings_pathes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Bitmap> entry = iter.next();
            if (!detail.contains(entry.getKey())) {
                entry.getValue().recycle();
                iter.remove();
            }
        }

        if (!spanStrings_pathes.isEmpty()) {
            File[] files = new File[spanStrings_pathes.size()];
            int count = 0;
            for (String id: spanStrings_pathes.keySet()) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                files[count++] = ImageUtil.persistImage(spanStrings_pathes.get(id), imageFileName);
            }
            try {
                params.put("file", files);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return params;
    }


    public static void sendMultipartRequest(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        params.setForceMultipartEntityContentType(true);
        client.post(getAbsoluteUrl(url), params, asyncHttpResponseHandler);
    }

    public static void sendRequest(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.post(getAbsoluteUrl(url), params, asyncHttpResponseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}