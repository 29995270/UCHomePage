package com.freeze.uchomepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

public class MainActivity extends AppCompatActivity {

    private HomePageAdapter adapter;
    private SearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        searchBar = (SearchBar) findViewById(R.id.search_bar);

        adapter = new HomePageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(searchBar);
        viewPager.setCurrentItem(1);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                ((NewsPageFragment) adapter.fragments.get(0)).getDragTrack().addDragActionReceiver(searchBar);
            }
        });
    }

    private class HomePageAdapter extends FragmentStatePagerAdapter {

        public SparseArray<Fragment> fragments = new SparseArray<>(2);

        HomePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                fragments.append(1, new MainPageFragment());
                return fragments.get(1);
            }
            if (position == 0) {
                fragments.append(0, new NewsPageFragment());
                return fragments.get(0);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
