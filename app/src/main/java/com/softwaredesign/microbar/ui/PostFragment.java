package com.softwaredesign.microbar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.gson.reflect.TypeToken;
import com.softwaredesign.microbar.MyApplication;
import com.softwaredesign.microbar.R;
import com.softwaredesign.microbar.adapter.PostAdapter;
import com.softwaredesign.microbar.model.Post;
import com.softwaredesign.microbar.util.GsonUtil;
import com.softwaredesign.microbar.util.PostUtil;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 16/6/21.
 */
// 与帖子有关的Fragment
public class PostFragment extends Fragment {

    private static final int UPLOAD_POST = 0;

    private static final String GETMORE = "getPostList";
    private static final String SEARCHPOST = "searchPostList";
    private static final String RECENTLYWATCH = "getRecentPost";
    private static final String GETMYPOST = "getPostByMyself";

    public enum POSTTYPE {
        HOMEPAGE, HISTORY, MYPOST, SEARCH
    }

    private MyApplication myApplication;

    private FragmentActivity fragmentActivity;
    private SwipeRefreshLayout mRefreshLayout;
    private LoadMoreListView mLoadMoreListView;
    private PostAdapter postAdapter;
    private List<Post> posts = new ArrayList<>();
    private String title;
    private int tag;
    private int accountId;
    private POSTTYPE postType;

    private StringCallback callback;

    public PostFragment() {
    }

    public static PostFragment getPostFragment(POSTTYPE key) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("KEY", key);
        PostFragment postFragment = new PostFragment();
        postFragment.setArguments(arguments);
        return postFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_page, container, false);
        Bundle arguments = getArguments();
        // 决定获取哪种数据(首页/最近看过/我发的帖)
        postType = (POSTTYPE) arguments.getSerializable("KEY");
        setHasOptionsMenu(true);
        init(view);
        setListener();
        return view;
    }

    public void init(View v) {
        fragmentActivity = getActivity();
        myApplication = (MyApplication) fragmentActivity.getApplication();
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.mainPage);
        mLoadMoreListView = (LoadMoreListView) v.findViewById(R.id.mainListView);
        postAdapter = new PostAdapter(fragmentActivity, posts);
        mLoadMoreListView.setAdapter(postAdapter);
        callback =  new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) {
                List<Post> newPosts = GsonUtil.parseList(response, new TypeToken<List<Post>>() {
                }.getType());
                posts.addAll(newPosts);
                postAdapter.notifyDataSetChanged();
                mLoadMoreListView.setLoading(false);
            }
        };

        switch (postType) {
            case HOMEPAGE:
                PostUtil.lookForMore(GETMORE, 0, callback);
                break;
            case HISTORY:
                PostUtil.getHistory(RECENTLYWATCH, myApplication.getRecentlyWatches(), callback);
                break;
            case MYPOST:
                PostUtil.getMyPost(GETMYPOST, accountId, callback);
                break;
            case SEARCH:
                PostUtil.searchPost(SEARCHPOST, title, tag, callback);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainpage_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mainpage_search:
                intent = new Intent(fragmentActivity, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.mainpage_add:
                intent = new Intent(fragmentActivity, PostActivity.class);
                startActivityForResult(intent, UPLOAD_POST);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == FragmentActivity.RESULT_OK) {
            switch(requestCode) {
                case UPLOAD_POST:
                    Post post = (Post) data.getSerializableExtra("post");
                    posts.add(0, post);
                    postAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setListener() {
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //refreshForNew(mList.get(0).getPostId());
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        //上拉加载
        mLoadMoreListView.setOnLoadListener(new LoadMoreListView.OnLoadListener() {
            @Override
            public void onLoad() {
                PostUtil.lookForMore(GETMORE, posts.size(), callback);
            }
        });

        mLoadMoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("PostFragment", ""+parent.getWidth());

                Post post = posts.get(position);
                Bundle data = new Bundle();
                data.putString("postTitle", post.getTitle());
                data.putInt("postId", post.getPostId());
                data.putInt("width", parent.getWidth());
                if (postType == POSTTYPE.HOMEPAGE) {
                    myApplication.getRecentlyWatches().add(post.getPostId());
                }

                Intent intent = new Intent(fragmentActivity, FloorActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setSearchTitle(String searchTitle) {
        title = searchTitle;
    }

    public void setSearchTag(int searchTag) {
        tag = searchTag;
    }
}
