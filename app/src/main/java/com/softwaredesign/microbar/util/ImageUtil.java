package com.softwaredesign.microbar.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by mac on 16/6/3.
 */
public class ImageUtil {

    /**
     * @param options   参数
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return 压缩比率
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (reqWidth * reqHeight != 0 || height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            Log.d("ImageUtil", "radio is " + inSampleSize);
        }
        return inSampleSize;
    }

    /**
     * @param filePath  图片路径
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return 压缩后的图片
     */
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // 初步压缩
        // options的inSampleSize只能为2的幂,所有其它的值都会被近似成小于该值且最接近该值的2的幂
        Bitmap src = BitmapFactory.decodeFile(filePath, options);
        if (src != null) {
            Bitmap dst = createScaleBitmap(src, reqWidth, 0);
            if (src != dst) {
                src.recycle();
            }
            return dst;
        }
        return null;
    }

    /**
     *
     * @param res 资源对象
     * @param id 资源id
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 压缩后的图片
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int id, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeResource(res, id, options);
        if (src != null) {
            Bitmap dst = createScaleBitmap(src, reqWidth, 0);
            if (src != dst) {
                src.recycle();
            }
            return dst;
        }
        return null;
    }

    /**
     * @param src       待压缩图片
     * @param dstWidth  目标宽度
     * @param dstHeight 目标高度
     * @return 压缩后的图片(默认竖屏, 按传入的宽度进行等比例缩放)
     */
    public static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst;
        dstHeight = Math.round((float) dstWidth / (float) src.getWidth() * src.getHeight());
        // 最后一个参数为是否平滑,放大图片需要设置为true,缩小需要设置为false
        if (src.getWidth() < dstWidth) {
            dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
        } else {
            dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        }
        Log.d("ImageUtil", "dstWidth is " + dst.getWidth() + ", dstHeight is " + dst.getHeight());
        return dst;
    }

    public static Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int beginRate = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, beginRate, bOut);
        while(bOut.size()/1024 > 100) {
            beginRate -= 10;
            bOut.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, beginRate, bOut);
            if (beginRate == 0) break;
        }
        Log.d("ImageUtil", ""+beginRate);
        Log.d("ImageUtil", "" + bOut.size());
        ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
        Bitmap newBitmap = BitmapFactory.decodeStream(bIn);
        if (newBitmap != null) {
            bitmap.recycle();
            return newBitmap;
        } else {
            return bitmap;
        }
    }

    public static File persistImage(Bitmap bitmap, String imageFileName) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(imageFileName, ".jpg", SDCardUtil.getFileDir(SDCardUtil.FILEDIR+"/"+SDCardUtil.CACHE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("ImageUtil", tempFile.getAbsolutePath());
        OutputStream os;
        try {
            os = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    public static File storeUserPortrait(Bitmap bitmap, int accountId) {
        String path = SDCardUtil.getSdPath()+SDCardUtil.FILEDIR+"/"+SDCardUtil.CACHE+"/"+"user_portrait_"+ accountId + ".jpg";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        Log.d("ImageUtil", ""+file.exists());
        file = new File(path);
        try {
            file.createNewFile();
            Log.d("ImageUtil", ""+file.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
