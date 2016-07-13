package com.softwaredesign.microbar.util;

import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by mac on 16/6/23.
 */
public class PostUtil {
    private static final String BASE_URL = "http://119.29.178.68:8080/sysu-micro-bar/";
    //private static final String BASE_URL = "http://xxx.tunnel.qydev.com/sysu-micro-bar/";

    // 查看更多帖子
    public static void lookForMore(String url, int postTotal, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("currentPostNum", Integer.toString(postTotal))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void refreshForNew(String url, int firstPostId, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("firstPostId", Integer.toString(firstPostId))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void checkForNew(String url, int accountId, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("accountId", Integer.toString(accountId))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void searchPost(String url, String title, int tag, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("title", title)
                .addParams("tag", Integer.toString(tag))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void getHistory(String url, LinkedHashSet<Integer> history, Callback callback) {
        StringBuilder sb = new StringBuilder();
        Iterator iter = history.iterator();
        while (iter.hasNext()) {
            sb.insert(0,"["+iter.next()+"]");
        }
        Log.d("PostUtil", sb.toString());
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("postIds", sb.toString())
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void getMyPost(String url, int accountId, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("accountId", Integer.toString(accountId))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void modifyPersonalInfo(String url, int accountId, String nickname, String newPassword, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("accountId", Integer.toString(accountId))
                .addParams("nickname", nickname)
                .addParams("newPassword", newPassword)
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    public static void getPostDetail(String url, int postId, Callback callback) {
        OkHttpUtils
                .post()
                .url(getAbsoluteUrl(url))
                .addParams("postId", Integer.toString(postId))
                .build()
                .connTimeOut(20000)
                .execute(callback);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
