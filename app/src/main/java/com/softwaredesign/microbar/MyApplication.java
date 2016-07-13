package com.softwaredesign.microbar;

import android.app.Application;
import android.util.Log;

import com.softwaredesign.microbar.util.SDCardUtil;

import java.util.LinkedHashSet;

/**
 * Created by mac on 16/7/2.
 */
public class MyApplication extends Application {

    private LinkedHashSet<Integer> recentlyWatches;

    @Override
    public void onCreate() {
        recentlyWatches = new LinkedHashSet<>();
        if (SDCardUtil.checkSdCard()) {
            SDCardUtil.createFileDir(SDCardUtil.FILEDIR);
            SDCardUtil.createFileDir(SDCardUtil.FILEDIR+"/"+SDCardUtil.FILEPHOTO);
            SDCardUtil.createFileDir(SDCardUtil.FILEDIR+"/"+SDCardUtil.CACHE);
        } else {
            Log.d("MyApplication", "创建失败");
        }
        super.onCreate();
    }

    public LinkedHashSet<Integer> getRecentlyWatches() {
        return recentlyWatches;
    }

    public void setRecentlyWatches(LinkedHashSet<Integer> recentlyWatches) {
        this.recentlyWatches = recentlyWatches;
    }
}
