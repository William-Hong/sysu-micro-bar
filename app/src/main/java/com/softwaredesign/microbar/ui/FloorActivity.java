package com.softwaredesign.microbar.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.adapter.FloorAdapter;
import com.softwaredesign.microbar.model.Floor;
import com.softwaredesign.microbar.util.GsonUtil;
import com.softwaredesign.microbar.util.PostUtil;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.callback.StringCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 16/6/3.
 */
public class FloorActivity extends AppCompatActivity {
    private static final int MAKE_REPLY = 0;
    private static final int CREATE_FLOOR = 1;
    private static final String GETPOSTDETAIL = "getPostDetail";

    private int postId;
    private Toolbar floorToolbar;
    private TextView title;
    private LoadMoreListView container;
    private FloorAdapter floorAdapter;
    private List<Floor> floors;
    MyHandler myHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        WeakReference<FloorActivity> mActivity;
        MyHandler(FloorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FloorActivity floorActivity = mActivity.get();
            if (floorActivity == null)
                return;
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent(floorActivity, ReplyActivity.class);
                    Bundle bundle = msg.getData();
                    bundle.putInt("type", MAKE_REPLY);
                    intent.putExtras(bundle);
                    floorActivity.startActivityForResult(intent, MAKE_REPLY);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);

        Bundle bundle = getIntent().getExtras();
        floorToolbar = (Toolbar) findViewById(R.id.floorToolbar);
        floorToolbar.setTitle("帖子详情");
        floorToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        setSupportActionBar(floorToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (TextView) findViewById(R.id.title);
        title.setText(bundle.getString("postTitle"));
        postId = bundle.getInt("postId");
        Log.d("FloorActivity", ""+postId);
        int width = bundle.getInt("width");
        container = (LoadMoreListView) findViewById(R.id.container);
        floors = new ArrayList<>();
        floorAdapter = new FloorAdapter(this, floors, width, postId);
        floorAdapter.setHandler(myHandler);
        container.setAdapter(floorAdapter);
        getPostDetail();
        container.setOnScrollListener(new FloorScrollListener(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            switch(requestCode) {
                case MAKE_REPLY:
                    break;
                case CREATE_FLOOR:
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.floor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.makeComment:
                Intent intent = new Intent(FloorActivity.this, ReplyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("type", CREATE_FLOOR);
                bundle.putInt("postId", postId);
                intent.putExtras(bundle);
                startActivityForResult(intent, CREATE_FLOOR);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPostDetail() {
        PostUtil.getPostDetail(GETPOSTDETAIL, postId, new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                Log.d("FloorActivity", response);
                List<Floor> newFloors = GsonUtil.parseList(response, new TypeToken<List<Floor>>() {
                }.getType());
                for (int i = 0; i < newFloors.size(); i++) {
                    Log.d("FloorActivity", newFloors.get(i).toString());
                }
                floors.addAll(newFloors);
                floorAdapter.notifyDataSetChanged();
            }
        });
    }

    class FloorScrollListener implements AbsListView.OnScrollListener {
        private final Context context;

        public FloorScrollListener(Context context) {
            this.context = context;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            final Picasso picasso = Picasso.with(context);
            if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                picasso.resumeTag(context);
            } else {
                picasso.pauseTag(context);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // Do nothing.
        }
    }
}
