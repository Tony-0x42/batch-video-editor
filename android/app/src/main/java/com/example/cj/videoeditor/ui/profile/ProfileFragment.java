package com.example.cj.videoeditor.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.activity.AgreementActivity;
import com.example.cj.videoeditor.activity.CustomerServiceActivity;
import com.example.cj.videoeditor.activity.EditProfileActivity;
import com.example.cj.videoeditor.activity.LoginActivity;
import com.example.cj.videoeditor.activity.PrivacyActivity;
import com.example.cj.videoeditor.bean.User;
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.PageApiCallback;
import com.example.cj.videoeditor.network.dto.BatchAppVersionDto;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;
import com.example.cj.videoeditor.network.util.TokenManager;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.PhoneUtil;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;
import java.io.File;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    ApiService apiService;
    @Inject
    TokenManager tokenManager;

    private TextView tvName, tvPhone, tvVipExpire;
    private Button btnLogout;
    private ProgressBar progressBar;

    private static final int PLATFORM_ANDROID = 1;
    private static final int STATUS_ENABLED = 0;
    private static final int UPDATE_TYPE_FORCE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvName = view.findViewById(R.id.tv_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvVipExpire = view.findViewById(R.id.tv_vip_expire);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);

        View header = view.findViewById(R.id.header);
        header.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        setupMenu(view, R.id.menu_customer_service, getString(R.string.customer_service), "", v -> startActivity(new Intent(getContext(), CustomerServiceActivity.class)));
        setupMenu(view, R.id.menu_douyin, getString(R.string.douyin_account), "", v -> ToastUtil.show(getContext(), R.string.coming_soon));
        setupMenu(view, R.id.menu_wechat, getString(R.string.bind_wechat), "", v -> ToastUtil.show(getContext(), R.string.coming_soon));
        setupMenu(view, R.id.menu_switch, getString(R.string.switch_account), "", v -> ToastUtil.show(getContext(), R.string.coming_soon));
        setupMenu(view, R.id.menu_delete_account, getString(R.string.logout_account), "", v -> confirmDeleteAccount());
        setupMenu(view, R.id.menu_agreement, getString(R.string.user_agreement_menu), "", v -> startActivity(new Intent(getContext(), AgreementActivity.class)));
        setupMenu(view, R.id.menu_privacy, getString(R.string.privacy_policy_menu), "", v -> startActivity(new Intent(getContext(), PrivacyActivity.class)));
        setupMenu(view, R.id.menu_clear_cache, getString(R.string.clear_cache), getCacheSize(), v -> clearCache());
        setupMenu(view, R.id.menu_check_update, getString(R.string.check_update), "", v -> checkForUpdate());
        setupMenu(view, R.id.menu_version, getString(R.string.current_version), getVersionName(), null);

        btnLogout.setOnClickListener(v -> logout());

        loadUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUser();
    }

    private void loadUser() {
        String phone = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_PHONE, "");
        if (phone.isEmpty()) {
            renderUserFromSp();
            return;
        }
        showLoading(true);
        apiService.getAppCustomer(phone).enqueue(new ApiCallback<BatchCustomerDto>() {
            @Override
            public void onSuccess(BatchCustomerDto data) {
                showLoading(false);
                if (data != null) {
                    UserStore.saveCustomerDto(requireContext(), data);
                }
                renderUserFromSp();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(getContext(), msg);
                renderUserFromSp();
            }
        });
    }

    private void renderUserFromSp() {
        String phone = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_PHONE, "");
        String name = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_NAME, "");
        boolean vip = SharedPrefUtil.getBoolean(requireContext(), AppConfig.SP_KEY_USER_VIP, false);
        String expire = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_VIP_EXPIRE, "");
        tvName.setText(name.isEmpty() ? getString(R.string.nickname) : name);
        tvPhone.setText(phone.isEmpty() ? "" : PhoneUtil.maskPhone(phone));
        tvVipExpire.setText(vip && !expire.isEmpty() ? getString(R.string.vip_expire_format, expire) : "");
    }

    private void setupMenu(View root, int menuId, String title, String value, View.OnClickListener listener) {
        View menu = root.findViewById(menuId);
        TextView tvTitle = menu.findViewById(R.id.tv_title);
        TextView tvExtra = menu.findViewById(R.id.tv_extra);
        tvTitle.setText(title);
        tvExtra.setText(value);
        if (listener != null) {
            menu.setOnClickListener(listener);
        }
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_logout)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    showLoading(true);
                    apiService.appLogout().enqueue(new ApiCallback<Object>() {
                        @Override
                        public void onSuccess(Object data) {
                            doLocalLogout();
                        }

                        @Override
                        public void onError(String msg) {
                            ToastUtil.show(getContext(), msg);
                            doLocalLogout();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void doLocalLogout() {
        showLoading(false);
        tokenManager.clearToken();
        SharedPrefUtil.clear(requireContext());
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_account)
                .setPositiveButton(R.string.confirm, (dialog, which) -> doLocalLogout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private String getCacheSize() {
        long size = getFolderSize(requireContext().getCacheDir());
        File externalCache = requireContext().getExternalCacheDir();
        if (externalCache != null) {
            size += getFolderSize(externalCache);
        }
        return String.format("%.2fMB", size / 1024.0 / 1024.0);
    }

    private long getFolderSize(File file) {
        long size = 0;
        if (file == null || !file.exists()) return 0;
        File[] files = file.listFiles();
        if (files == null) return file.length();
        for (File child : files) {
            size += child.isDirectory() ? getFolderSize(child) : child.length();
        }
        return size;
    }

    private void clearCache() {
        try {
            deleteDir(requireContext().getCacheDir());
            File externalCache = requireContext().getExternalCacheDir();
            if (externalCache != null) deleteDir(externalCache);
            ToastUtil.show(getContext(), R.string.clear_cache_success);
        } catch (Exception e) {
            ToastUtil.show(getContext(), "清理失败，请重试");
        }
    }

    private boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) return true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File child : files) {
                if (child.isDirectory()) {
                    deleteDir(child);
                } else {
                    child.delete();
                }
            }
        }
        return dir.delete();
    }

    private String getVersionName() {
        try {
            return requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    private void checkForUpdate() {
        showLoading(true);
        apiService.getAppVersionList().enqueue(new PageApiCallback<BatchAppVersionDto>() {
            @Override
            public void onSuccess(long total, List<BatchAppVersionDto> rows) {
                showLoading(false);
                BatchAppVersionDto latest = findLatestAndroidVersion(rows);
                if (latest == null || latest.getVersionNo() == null) {
                    ToastUtil.show(getContext(), R.string.already_latest_version);
                    return;
                }
                String currentVersion = getVersionName();
                if (isNewerVersion(latest.getVersionNo(), currentVersion)) {
                    showUpdateDialog(latest);
                } else {
                    ToastUtil.show(getContext(), R.string.already_latest_version);
                }
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(getContext(), getString(R.string.version_check_failed, msg));
            }
        });
    }

    private BatchAppVersionDto findLatestAndroidVersion(List<BatchAppVersionDto> rows) {
        BatchAppVersionDto latest = null;
        for (BatchAppVersionDto item : rows) {
            if (item == null || item.getVersionNo() == null) continue;
            Integer platform = item.getPlatform();
            Integer status = item.getStatus();
            Integer updateType = item.getUpdateType();
            if (platform == null || platform != PLATFORM_ANDROID) continue;
            if (status == null || status != STATUS_ENABLED) continue;
            if (updateType == null || updateType == 3) continue;
            if (latest == null || isNewerVersion(item.getVersionNo(), latest.getVersionNo())) {
                latest = item;
            }
        }
        return latest;
    }

    private boolean isNewerVersion(String remote, String local) {
        if (remote == null) return false;
        if (local == null) return true;
        String[] remoteParts = remote.split("\\.");
        String[] localParts = local.split("\\.");
        int maxLength = Math.max(remoteParts.length, localParts.length);
        for (int i = 0; i < maxLength; i++) {
            int remotePart = i < remoteParts.length ? parseVersionPart(remoteParts[i]) : 0;
            int localPart = i < localParts.length ? parseVersionPart(localParts[i]) : 0;
            if (remotePart != localPart) {
                return remotePart > localPart;
            }
        }
        return false;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showUpdateDialog(BatchAppVersionDto version) {
        String message = getString(R.string.version_format, version.getVersionNo());
        if (version.getUpdateContent() != null && !version.getUpdateContent().trim().isEmpty()) {
            message += "\n\n" + version.getUpdateContent().trim();
        }
        boolean forceUpdate = version.getUpdateType() != null && version.getUpdateType() == UPDATE_TYPE_FORCE;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_version_found)
                .setMessage(message)
                .setPositiveButton(R.string.update_now, (dialog, which) -> openDownloadUrl(version.getDownloadUrl()));
        if (!forceUpdate) {
            builder.setNegativeButton(R.string.update_later, null);
        } else {
            builder.setCancelable(false);
        }
        builder.show();
    }

    private void openDownloadUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            ToastUtil.show(getContext(), "下载链接为空");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtil.show(getContext(), "无法打开下载链接");
        }
    }
}
