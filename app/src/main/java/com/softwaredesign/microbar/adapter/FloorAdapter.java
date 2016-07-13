package com.softwaredesign.microbar.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.model.Floor;
import com.softwaredesign.microbar.util.ImageUtil;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

/**
 * Created by mac on 16/6/3.
 */
public class FloorAdapter extends BaseAdapter {

    private Context mContext;
    private List<Floor> floors;
    private LayoutInflater mInflater;
    private Resources mResources;
    private LruCache<String, Bitmap> mCaches;
    private int postId;
    private int bitmapWidth;

    private Handler mHandler;

    public FloorAdapter() {
    }

    public FloorAdapter(Context context, List<Floor> list, int maxWidth, int postId) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        floors = list;
        mResources = mContext.getResources();
        bitmapWidth = maxWidth;
        this.postId = postId;

        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        mCaches = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
                //return super.sizeOf(key, value);
            }
        };
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public Object getItem(int position) {
        return floors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.floor_detail, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.headPortait = (ImageView) convertView.findViewById(R.id.headPortrait);
            viewHolder.nickName = (TextView) convertView.findViewById(R.id.nickName);
            viewHolder.reply = (Button) convertView.findViewById(R.id.reply);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.showReply = (TextView) convertView.findViewById(R.id.showReply);
            viewHolder.showReply.setVisibility(View.INVISIBLE);
            viewHolder.floorContent = (TextView) convertView.findViewById(R.id.floorContent);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Floor floor = (Floor) getItem(position);

        // 加载用户头像
        Picasso.with(mContext)
                .load(floor.getHeadImageUrl())
                .placeholder(R.drawable.default_portrait)  //默认(加载前)头像
                .error(R.drawable.default_portrait)  //加载失败时的头像
                .resizeDimen(R.dimen.portrait_width, R.dimen.portrait_height)
                .centerInside()
                .tag(mContext)
                .into(viewHolder.headPortait);

        viewHolder.nickName.setText(floor.getNickname());
        viewHolder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("postId", postId);
                bundle.putString("nickName", floor.getNickname());
                bundle.putInt("replyFloorId", floor.getFloorId());
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        });

        viewHolder.time.setText(floor.getCreateTime());
        Log.d("FloorAdapter", ""+floor.getReplyed());
        if (floor.getReplyed() == 1) {
            viewHolder.showReply.setVisibility(View.VISIBLE);
            String replyWho = floor.getReplyWho();
            viewHolder.showReply.setText("回复 "+ replyWho + ":");
        } else {
            viewHolder.showReply.setVisibility(View.INVISIBLE);
        }
        // 异步加载floorContent
        Spanned spanned = Html.fromHtml(floor.getDetail(), new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                LevelListDrawable levelListDrawable = new LevelListDrawable();
                Drawable loadingImage;
//                // 未加载前设置的默认图片
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    loadingImage = mContext.getDrawable(R.drawable.default_portrait);
//                } else {
//                    loadingImage = mContext.getResources().getDrawable(R.drawable.default_portrait);
//                }
                Bitmap defaultLoading = ImageUtil.decodeSampledBitmapFromResource(mResources, R.drawable.default_loading, bitmapWidth, bitmapWidth);
                loadingImage = new BitmapDrawable(mResources, defaultLoading);
                levelListDrawable.addLevel(0, 0, loadingImage);
                levelListDrawable.setBounds(0, 0, loadingImage.getIntrinsicWidth(), loadingImage.getIntrinsicHeight());
                Bitmap bitmap = getBitmapFromCache(source);
                if (bitmap == null) {
                    // 异步加载图片
                    new ImageGetterAsyncTask(mContext, source, levelListDrawable).execute(viewHolder.floorContent);
                } else {
                    Drawable drawable = new BitmapDrawable(mResources, bitmap);
                    levelListDrawable.addLevel(1, 1, drawable);
                    levelListDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    levelListDrawable.setLevel(1);
                }
                return levelListDrawable;
            }
        }, null);
        viewHolder.floorContent.setText(spanned);
        return convertView;
    }

    class ImageGetterAsyncTask extends AsyncTask<TextView, Void, Bitmap> {

        private LevelListDrawable levelListDrawable;
        private Context context;
        private String source;
        private TextView textView;

        public ImageGetterAsyncTask(Context context, String source, LevelListDrawable levelListDrawable) {
            this.context = context;
            this.source = source;
            this.levelListDrawable = levelListDrawable;
        }

        @Override
        protected Bitmap doInBackground(TextView... params) {
            textView = params[0];
            try {
                Log.d("FloorAdapter", "Downloading the image from " + source);
                return Picasso.with(context).load(source).tag(context).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Bitmap resizedBitmap = ImageUtil.createScaleBitmap(bitmap, bitmapWidth, 0);
                if (bitmap != resizedBitmap) {
                    bitmap.recycle();
                }
                addBitmapToCache(source, resizedBitmap);
                Drawable drawable = new BitmapDrawable(mResources, resizedBitmap);
                levelListDrawable.addLevel(1, 1, drawable);
                levelListDrawable.setBounds(0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
                levelListDrawable.setLevel(1);
                textView.setText(textView.getText());
            }
        }
    }

    static class ViewHolder {
        private ImageView headPortait;
        private TextView nickName;
        private Button reply;
        private TextView time;
        private TextView showReply;
        private TextView floorContent;
    }

    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapFromCache(url) == null) {
            mCaches.put(url, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String url) {
        return mCaches.get(url);
    }
}
