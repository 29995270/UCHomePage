package com.freeze.uchomepage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.freeze.uchomepage.pullrefresh.DoublePartRefreshDrawable;
import com.freeze.uchomepage.pullrefresh.PullRefreshLayout;

/**
 * Created by Administrator on 2016/10/16.
 */
public class NewsActivity extends AppCompatActivity{

    private View content;

    private int translationYDp = 244 + 100 - 80*2 - 64;
    private View root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        final PullRefreshLayout refreshLayout = (PullRefreshLayout) findViewById(R.id.refresh);
        root = findViewById(R.id.root);
        refreshLayout.setRefreshDrawable(new DoublePartRefreshDrawable(this, refreshLayout));

        content = findViewById(R.id.content);

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

            @Override
            public void onOverScroll() {
                Toast.makeText(NewsActivity.this, "overscroll", Toast.LENGTH_SHORT).show();

                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 2000);

                content.animate().translationY(Utils.dp2px(NewsActivity.this, translationYDp))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finish();
                            }
                        })
                        .start();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}
