package com.freeze.uchomepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DragTracker.DragActionReceiver {

    private HomePageAdapter adapter;
    private SearchBar searchBar;
    private ViewPager viewPager;
    private int dragOffset;
    private float dragUpPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        searchBar = (SearchBar) findViewById(R.id.search_bar);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "search bar", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new HomePageAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(searchBar);
        viewPager.setCurrentItem(1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                searchBar.setVisibility(View.VISIBLE);
                adapter.arcView.setWillNotDraw(true);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                adapter.dragTracker.addDragActionReceiver(searchBar);
                adapter.dragTracker.addDragActionReceiver(MainActivity.this);
                adapter.arcView.expendOrCollapse(true);
            }
        });
    }

    @Override
    public void onDrag(int dragDownX, int dragDownYOffset, float dragDownPercent) {
        this.dragOffset = dragDownYOffset;
        if (dragDownYOffset < 0) {
            searchBar.setVisibility(View.INVISIBLE);
            adapter.arcView.setWillNotDraw(false);

            float dragUp = Math.abs(dragDownYOffset);
            dragUpPercent = dragUp / DragUpReceiver.MAX_DRAG_UP_DIS;
            adapter.newsTopBar.setTranslationY(Math.min(0, -adapter.newsTopBarHeight * (1 - dragUpPercent)));

        } else {
            searchBar.setVisibility(View.VISIBLE);
            adapter.arcView.setWillNotDraw(true);
        }
    }

    @Override
    public void onRelease(int dragDownX, int dragDownYOffset) {

        adapter.dragTracker.animationRelease(new Runnable() {
            @Override
            public void run() {
                startActivityForResult(new Intent(MainActivity.this, NewsActivity.class), 0);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.dragTracker.initLocation();
    }

    private class HomePageAdapter extends PagerAdapter {

        int newsTopBarHeight = Utils.dp2px(MainActivity.this, 64);

        SearchBar arcView;
        DragTracker dragTracker;
        DragUpReceiver dragUpReceiver;
        View newsTopBar;
        View newsList;

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            if (position == 0) {
                View newsPage = inflater.inflate(R.layout.news_page, container, false);
                container.addView(newsPage);
                arcView = (SearchBar) newsPage.findViewById(R.id.arc_view);
                arcView.setWillNotDraw(true);
                dragTracker = (DragTracker) newsPage.findViewById(R.id.drag_tracker);
                dragUpReceiver = (DragUpReceiver) newsPage.findViewById(R.id.drag_up_receiver);
                dragTracker.addDragActionReceiver(arcView);
                dragTracker.addDragActionReceiver(dragUpReceiver);
                viewPager.findViewById(R.id.some_menu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "some menu", Toast.LENGTH_SHORT).show();
                    }
                });

                newsTopBar = newsPage.findViewById(R.id.news_top_bar);
                newsTopBar.setTranslationY(-newsTopBarHeight);

                newsList = newsPage.findViewById(R.id.news_list);

                return newsPage;
            } else if (position == 1) {
                View mainPage = inflater.inflate(R.layout.main_page, container, false);
                container.addView(mainPage);
                return mainPage;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));

            if (position == 0) {
                arcView = null;
                dragTracker = null;
            } else if (position == 1) {

            }
        }
    }
}
