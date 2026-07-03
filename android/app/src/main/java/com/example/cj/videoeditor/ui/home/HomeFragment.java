package com.example.cj.videoeditor.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.BrandActivity;
import com.example.cj.videoeditor.activity.ContactActivity;
import com.example.cj.videoeditor.activity.DocumentActivity;
import com.example.cj.videoeditor.activity.LearningActivity;
import com.example.cj.videoeditor.adapter.BannerAdapter;
import com.example.cj.videoeditor.adapter.HomeMenuAdapter;
import com.example.cj.videoeditor.model.Announcement;
import com.example.cj.videoeditor.model.Banner;
import com.example.cj.videoeditor.model.HomeMenu;
import com.example.cj.videoeditor.utils.MockData;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private LinearLayout indicatorContainer;
    private TextView tvAnnouncementTitle, tvAnnouncementChampion;
    private RecyclerView recyclerMenu;
    private List<Banner> banners = new ArrayList<>();
    private BannerAdapter bannerAdapter;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPagerBanner = view.findViewById(R.id.view_pager_banner);
        indicatorContainer = view.findViewById(R.id.indicator_container);
        tvAnnouncementTitle = view.findViewById(R.id.tv_announcement_title);
        tvAnnouncementChampion = view.findViewById(R.id.tv_announcement_champion);
        recyclerMenu = view.findViewById(R.id.recycler_menu);
        view.findViewById(R.id.ll_tutorial).setOnClickListener(v -> startActivity(new Intent(getContext(), DocumentActivity.class)));

        loadBanners();
        loadAnnouncement();
        loadMenus();
        startAutoScroll();
    }

    private void loadBanners() {
        banners = MockData.getBanners();
        bannerAdapter = new BannerAdapter(banners);
        viewPagerBanner.setAdapter(bannerAdapter);
        setupIndicator();
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
            }
        });
    }

    private void setupIndicator() {
        indicatorContainer.removeAllViews();
        for (int i = 0; i < banners.size(); i++) {
            View dot = new View(getContext());
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(size / 2, 0, size / 2, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
            indicatorContainer.addView(dot);
        }
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            indicatorContainer.getChildAt(i).setBackgroundResource(i == position ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }

    private void startAutoScroll() {
        autoScrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (banners.size() == 0) return;
                int next = (viewPagerBanner.getCurrentItem() + 1) % banners.size();
                viewPagerBanner.setCurrentItem(next, true);
                autoScrollHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void loadAnnouncement() {
        Announcement a = MockData.getAnnouncement();
        tvAnnouncementTitle.setText(a.title);
        tvAnnouncementChampion.setText(String.format("%s - ¥%s", a.championName, a.amount));
    }

    private void loadMenus() {
        List<HomeMenu> menus = MockData.getHomeMenus();
        recyclerMenu.setLayoutManager(new GridLayoutManager(getContext(), 4));
        HomeMenuAdapter adapter = new HomeMenuAdapter(menus);
        recyclerMenu.setAdapter(adapter);
        adapter.setOnItemClickListener((menu, position) -> {
            switch (position) {
                case 0:
                    startActivity(new Intent(getContext(), LearningActivity.class));
                    break;
                case 1:
                    startActivity(new Intent(getContext(), ContactActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(getContext(), BrandActivity.class));
                    break;
                default:
                    ToastUtil.show(getContext(), R.string.coming_soon);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoScrollHandler.removeCallbacksAndMessages(null);
    }
}
