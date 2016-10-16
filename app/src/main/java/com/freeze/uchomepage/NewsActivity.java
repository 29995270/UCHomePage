package com.freeze.uchomepage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.freeze.uchomepage.pullrefresh.DoublePartRefreshDrawable;
import com.freeze.uchomepage.pullrefresh.PullRefreshLayout;

/**
 * Created by Administrator on 2016/10/16.
 */
public class NewsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        final PullRefreshLayout refreshLayout = (PullRefreshLayout) findViewById(R.id.refresh);
        refreshLayout.setRefreshDrawable(new DoublePartRefreshDrawable(this, refreshLayout));

        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
