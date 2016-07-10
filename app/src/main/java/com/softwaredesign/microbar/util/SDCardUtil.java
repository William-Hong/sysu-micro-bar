package com.softwaredesign.microbar.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by mac on 16/7/9.
 */
public class SDCardUtil {
    public static final String FILEDIR = "/microBar";
    public static final String FILEPHOTO = "/photo";
    public static final String CACHE = "/cache";

    public static boolean checkSdCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    public static String getSdPath() {
        return Environment.getExternalStorageDirectory()+"/";
    }

    public static void createFileDir(String fileDir) {
        String path = getSdPath()+fileDir;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static File getFileDir(String fileDir) {
        String path = getSdPath()+fileDir;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
