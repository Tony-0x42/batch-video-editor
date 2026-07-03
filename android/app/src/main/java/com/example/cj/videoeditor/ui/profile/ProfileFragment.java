package com.example.cj.videoeditor.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.MockDataProvider;
import com.example.cj.videoeditor.utils.PhoneUtil;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.io.File;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvPhone, tvVipExpire;
    private Button btnLogout;

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
        setupMenu(view, R.id.menu_check_update, getString(R.string.check_update), "", v -> ToastUtil.show(getContext(), R.string.coming_soon));
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
        String name = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_NAME, "");
        boolean vip = SharedPrefUtil.getBoolean(requireContext(), AppConfig.SP_KEY_USER_VIP, false);
        String expire = SharedPrefUtil.getString(requireContext(), AppConfig.SP_KEY_USER_VIP_EXPIRE, "");
        if (phone.isEmpty()) {
            User mock = MockDataProvider.getUser();
            phone = mock.getPhone();
            name = mock.getName();
            vip = mock.isVip();
            expire = mock.getVipExpire();
        }
        tvName.setText(name);
        tvPhone.setText(PhoneUtil.maskPhone(phone));
        tvVipExpire.setText(vip ? getString(R.string.vip_expire_format, expire) : "");
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
                    SharedPrefUtil.clear(requireContext());
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_account)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    SharedPrefUtil.clear(requireContext());
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
}
