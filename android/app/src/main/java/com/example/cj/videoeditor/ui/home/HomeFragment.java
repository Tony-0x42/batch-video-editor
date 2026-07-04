package com.example.cj.videoeditor.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.BrandActivity;
import com.example.cj.videoeditor.activity.ContactActivity;
import com.example.cj.videoeditor.activity.DocumentActivity;
import com.example.cj.videoeditor.activity.LearningActivity;
import com.example.cj.videoeditor.activity.NoticeListActivity;
import com.example.cj.videoeditor.network.dto.BatchAppNoticeDto;
import com.example.cj.videoeditor.adapter.BannerAdapter;
import com.example.cj.videoeditor.adapter.HomeMenuAdapter;
import com.example.cj.videoeditor.bean.Announcement;
import com.example.cj.videoeditor.bean.Banner;
import com.example.cj.videoeditor.bean.HomeMenu;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.PageApiCallback;
import com.example.cj.videoeditor.network.dto.BatchHomeBannerDto;
import com.example.cj.videoeditor.network.dto.BatchHomeEntryDto;
import com.example.cj.videoeditor.network.dto.BatchHomeNewsDto;
import com.example.cj.videoeditor.network.dto.BatchHomeTutorialEntryDto;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    ApiService apiService;

    private LinearLayout bannerContainer;
    private ViewPager2 viewPagerBanner;
    private LinearLayout indicatorContainer;
    private TextView tvAnnouncementTitle, tvAnnouncementChampion;
    private RecyclerView recyclerMenu;
    private ProgressBar progressBar;
    private LinearLayout llTutorial;
    private LinearLayout llNotice;
    private TextView tvNoticeTitle;
    private View viewNoticeDot;
    private ImageView ivTutorialCover;
    private TextView tvTutorialTitle;
    private List<Banner> banners = new ArrayList<>();
    private BannerAdapter bannerAdapter;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private int pendingRequests = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bannerContainer = view.findViewById(R.id.banner_container);
        viewPagerBanner = view.findViewById(R.id.view_pager_banner);
        indicatorContainer = view.findViewById(R.id.indicator_container);
        tvAnnouncementTitle = view.findViewById(R.id.tv_announcement_title);
        tvAnnouncementChampion = view.findViewById(R.id.tv_announcement_champion);
        recyclerMenu = view.findViewById(R.id.recycler_menu);
        progressBar = view.findViewById(R.id.progress_bar);
        llTutorial = view.findViewById(R.id.ll_tutorial);
        ivTutorialCover = view.findViewById(R.id.iv_tutorial_cover);
        tvTutorialTitle = view.findViewById(R.id.tv_tutorial_title);
        llNotice = view.findViewById(R.id.ll_notice);
        tvNoticeTitle = view.findViewById(R.id.tv_notice_title);
        viewNoticeDot = view.findViewById(R.id.view_notice_dot);

        loadBanners();
        loadAnnouncement();
        loadMenus();
        loadNoticeEntry();
        loadTutorialEntry();
        startAutoScroll();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void startRequest() {
        pendingRequests++;
        showLoading(true);
    }

    private void finishRequest() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            pendingRequests = 0;
            showLoading(false);
        }
    }

    private void loadBanners() {
        startRequest();
        apiService.getHomeBannerList().enqueue(new PageApiCallback<BatchHomeBannerDto>() {
            @Override
            public void onSuccess(long total, List<BatchHomeBannerDto> rows) {
                finishRequest();
                banners = new ArrayList<>();
                for (BatchHomeBannerDto dto : rows) {
                    banners.add(new Banner(
                            dto.getBannerId() != null ? String.valueOf(dto.getBannerId()) : "",
                            dto.getImageUrl() != null ? dto.getImageUrl() : "",
                            dto.getLinkUrl() != null ? dto.getLinkUrl() : "",
                            dto.getSortWeight() != null ? dto.getSortWeight() : 0,
                            dto.getTitle() != null ? dto.getTitle() : ""
                    ));
                }
                if (banners.isEmpty()) {
                    bannerContainer.setVisibility(View.GONE);
                    return;
                }
                bannerContainer.setVisibility(View.VISIBLE);
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

            @Override
            public void onError(String msg) {
                finishRequest();
                ToastUtil.show(getContext(), msg);
                bannerContainer.setVisibility(View.GONE);
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
        startRequest();
        apiService.getHomeNewsList().enqueue(new PageApiCallback<BatchHomeNewsDto>() {
            @Override
            public void onSuccess(long total, List<BatchHomeNewsDto> rows) {
                finishRequest();
                BatchHomeNewsDto dto = rows.isEmpty() ? null : rows.get(0);
                if (dto == null) {
                    tvAnnouncementTitle.setText("");
                    tvAnnouncementChampion.setText("");
                    return;
                }
                Announcement a = new Announcement(
                        dto.getNewsTitle() != null ? dto.getNewsTitle() : "",
                        dto.getChampionName() != null ? dto.getChampionName() : "",
                        dto.getSalesAmount() != null ? String.valueOf(dto.getSalesAmount().intValue()) : "0"
                );
                tvAnnouncementTitle.setText(a.title);
                tvAnnouncementChampion.setText(String.format("%s - ¥%s", a.championName, a.amount));
            }

            @Override
            public void onError(String msg) {
                finishRequest();
                ToastUtil.show(getContext(), msg);
            }
        });
    }

    private void loadNoticeEntry() {
        startRequest();
        apiService.getNoticeList().enqueue(new PageApiCallback<BatchAppNoticeDto>() {
            @Override
            public void onSuccess(long total, List<BatchAppNoticeDto> rows) {
                finishRequest();
                if (rows == null || rows.isEmpty()) {
                    llNotice.setVisibility(View.GONE);
                    return;
                }
                BatchAppNoticeDto dto = rows.get(0);
                String title = dto.getNoticeTitle() != null ? dto.getNoticeTitle() : "";
                tvNoticeTitle.setText(title);
                viewNoticeDot.setVisibility(View.VISIBLE);
                llNotice.setOnClickListener(v -> startActivity(new Intent(getContext(), NoticeListActivity.class)));
                llNotice.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String msg) {
                finishRequest();
                llNotice.setVisibility(View.GONE);
            }
        });
    }

    private void loadMenus() {
        startRequest();
        apiService.getHomeEntryList().enqueue(new PageApiCallback<BatchHomeEntryDto>() {
            @Override
            public void onSuccess(long total, List<BatchHomeEntryDto> rows) {
                finishRequest();
                List<HomeMenu> menus = new ArrayList<>();
                for (BatchHomeEntryDto dto : rows) {
                    String name = dto.getEntryName() != null ? dto.getEntryName() : "";
                    menus.add(new HomeMenu(name, resolveDefaultIcon(name), dto.getIconUrl(), dto.getTargetType(), dto.getTargetValue()));
                }
                if (menus.isEmpty()) {
                    recyclerMenu.setVisibility(View.GONE);
                    return;
                }
                recyclerMenu.setVisibility(View.VISIBLE);
                recyclerMenu.setLayoutManager(new GridLayoutManager(getContext(), 4));
                HomeMenuAdapter adapter = new HomeMenuAdapter(menus, (menu, position) -> handleMenuClick(menu));
                recyclerMenu.setAdapter(adapter);
            }

            @Override
            public void onError(String msg) {
                finishRequest();
                ToastUtil.show(getContext(), msg);
            }
        });
    }

    private void handleMenuClick(HomeMenu menu) {
        String targetType = menu.getTargetType();
        String targetValue = menu.getTargetValue() != null ? menu.getTargetValue() : "";
        if ("2".equals(targetType)) {
            openUrl(targetValue);
            return;
        }
        if (!"1".equals(targetType)) {
            ToastUtil.show(getContext(), R.string.coming_soon);
            return;
        }
        Intent intent = null;
        if (targetValue.contains("learning")) {
            intent = new Intent(getContext(), LearningActivity.class);
        } else if (targetValue.contains("contact")) {
            intent = new Intent(getContext(), ContactActivity.class);
        } else if (targetValue.contains("brand")) {
            intent = new Intent(getContext(), BrandActivity.class);
        } else if (targetValue.contains("document") || targetValue.contains("tutorial")) {
            intent = new Intent(getContext(), DocumentActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        } else {
            ToastUtil.show(getContext(), R.string.coming_soon);
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) {
            ToastUtil.show(getContext(), "链接为空");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtil.show(getContext(), "无法打开链接");
        }
    }

    private void loadTutorialEntry() {
        startRequest();
        apiService.getHomeTutorialEntryList().enqueue(new PageApiCallback<BatchHomeTutorialEntryDto>() {
            @Override
            public void onSuccess(long total, List<BatchHomeTutorialEntryDto> rows) {
                finishRequest();
                if (rows == null || rows.isEmpty()) {
                    llTutorial.setVisibility(View.GONE);
                    return;
                }
                BatchHomeTutorialEntryDto dto = rows.get(0);
                String title = dto.getTitle() != null ? dto.getTitle() : "";
                String coverUrl = dto.getCoverUrl() != null ? dto.getCoverUrl() : "";
                Long documentId = dto.getDocumentId();
                tvTutorialTitle.setText(title);
                if (!coverUrl.isEmpty()) {
                    Glide.with(HomeFragment.this)
                            .load(coverUrl)
                            .placeholder(R.drawable.ic_learning)
                            .error(R.drawable.ic_learning)
                            .into(ivTutorialCover);
                } else {
                    ivTutorialCover.setImageResource(R.drawable.ic_learning);
                }
                llTutorial.setOnClickListener(v -> {
                    if (documentId == null) {
                        ToastUtil.show(getContext(), "未关联文档");
                        return;
                    }
                    Intent intent = new Intent(getContext(), DocumentActivity.class);
                    intent.putExtra(DocumentActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
                llTutorial.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String msg) {
                finishRequest();
                llTutorial.setVisibility(View.GONE);
            }
        });
    }

    private int resolveDefaultIcon(String name) {
        if (name == null) return R.drawable.ic_home;
        if (name.contains("学习")) return R.drawable.ic_learning;
        if (name.contains("信息") || name.contains("咨询")) return R.drawable.ic_contact;
        if (name.contains("品牌")) return R.drawable.ic_brand;
        if (name.contains("其他")) return R.drawable.ic_other_service;
        return R.drawable.ic_home;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoScrollHandler.removeCallbacksAndMessages(null);
    }
}
