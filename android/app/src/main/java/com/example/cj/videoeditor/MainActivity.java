package com.example.cj.videoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.cj.videoeditor.activity.LoginActivity;

import javax.inject.Inject;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.ui.home.HomeFragment;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.ui.aicreation.AiCreationFragment;
import com.example.cj.videoeditor.ui.watermark.WatermarkFragment;
import com.example.cj.videoeditor.ui.profile.ProfileFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    ApiService apiService;

    private LinearLayout bottomNavigation;
    private Fragment currentFragment;
    private int selectedTab = 0;
    private long lastBackPressTime = 0;

    private final int[] navIds = {R.id.nav_home, R.id.nav_ai_creation, R.id.nav_watermark, R.id.nav_profile};
    private final int[] navIcons = {R.drawable.ic_home, R.drawable.ic_ai_creation, R.drawable.ic_watermark, R.drawable.ic_profile};
    private final int[] navLabels = {R.string.nav_home, R.string.nav_ai_creation, R.string.nav_watermark, R.string.nav_profile};
    private final Fragment[] navFragments = {new HomeFragment(), new AiCreationFragment(), new WatermarkFragment(), new ProfileFragment()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SharedPrefUtil.getBoolean(this, AppConfig.SP_KEY_IS_LOGIN, false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        initBottomNavigation();
        AppConfig.loadGlobalConfig(this, apiService);

        if (savedInstanceState == null) {
            switchFragment(0);
        }
    }

    private void initBottomNavigation() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < navIds.length; i++) {
            View itemView = inflater.inflate(R.layout.item_bottom_nav, bottomNavigation, false);
            itemView.setId(navIds[i]);
            ImageView ivIcon = itemView.findViewById(R.id.iv_icon);
            TextView tvLabel = itemView.findViewById(R.id.tv_label);
            ivIcon.setImageResource(navIcons[i]);
            tvLabel.setText(navLabels[i]);
            final int index = i;
            itemView.setOnClickListener(v -> switchFragment(index));
            bottomNavigation.addView(itemView);
        }
        updateSelectedTab(0);
    }

    private void switchFragment(int index) {
        selectedTab = index;
        currentFragment = navFragments[index];
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, currentFragment)
                .commit();
        updateSelectedTab(index);
    }

    private void updateSelectedTab(int index) {
        int selectedColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int unselectedColor = ContextCompat.getColor(this, R.color.textSecondary);
        for (int i = 0; i < bottomNavigation.getChildCount(); i++) {
            View itemView = bottomNavigation.getChildAt(i);
            ImageView ivIcon = itemView.findViewById(R.id.iv_icon);
            TextView tvLabel = itemView.findViewById(R.id.tv_label);
            boolean selected = i == index;
            ivIcon.setColorFilter(selected ? selectedColor : unselectedColor);
            tvLabel.setTextColor(selected ? selectedColor : unselectedColor);
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressTime > 2000) {
            lastBackPressTime = currentTime;
            ToastUtil.show(this, R.string.press_again_to_exit);
        } else {
            super.onBackPressed();
        }
    }

    public void selectTab(int tabId) {
        for (int i = 0; i < navIds.length; i++) {
            if (navIds[i] == tabId) {
                switchFragment(i);
                return;
            }
        }
    }
}
