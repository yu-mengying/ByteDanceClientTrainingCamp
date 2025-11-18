package com.example.bytedancefollowpage;

import android.os.Bundle;

import com.example.bytedancefollowpage.fragment.FollowerFragment;
import com.example.bytedancefollowpage.fragment.FollowingFragment;
import com.example.bytedancefollowpage.fragment.FriendFragment;
import com.example.bytedancefollowpage.fragment.MutualFragment;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bytedancefollowpage.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String[] tabTitles = {"互关", "关注", "粉丝", "朋友"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new SocialFragmentAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    class SocialFragmentAdapter extends FragmentStateAdapter {
        public SocialFragmentAdapter(FragmentActivity fa) { super(fa); }
        @Override public int getItemCount() { return tabTitles.length; }
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new MutualFragment();
                case 1: return new FollowingFragment();
                case 2: return new FollowerFragment();
                case 3: return new FriendFragment();
                default: return new FollowingFragment();
            }
        }
    }
}