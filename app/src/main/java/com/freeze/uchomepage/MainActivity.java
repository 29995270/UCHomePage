package com.freeze.uchomepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        SearchBar searchBar = (SearchBar) findViewById(R.id.search_bar);

        viewPager.setAdapter(new HomePageAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(searchBar);
        viewPager.setCurrentItem(1);
    }

    private class HomePageAdapter extends FragmentStatePagerAdapter {

        HomePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) return new MainPageFragment();
            if (position == 0) return new NewsPageFragment();
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
