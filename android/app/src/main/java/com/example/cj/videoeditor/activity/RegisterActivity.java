package com.example.cj.videoeditor.activity;

import dagger.hilt.android.AndroidEntryPoint;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.cj.videoeditor.MainActivity;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.AuthApiCallback;
import com.example.cj.videoeditor.network.dto.AppRegisterBody;
import com.example.cj.videoeditor.network.dto.BatchCustomerDto;
import com.example.cj.videoeditor.network.util.TokenManager;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.PhoneUtil;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

@AndroidEntryPoint
public class RegisterActivity extends BaseActivity {

    @Inject
    ApiService apiService;
    @Inject
    TokenManager tokenManager;

    private TextView tvParentPhone;
    private TextView btnScanQrcode;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private LinearLayout llPhone;
    private LinearLayout llPassword;
    private LinearLayout llConfirmPassword;
    private ImageView ivTogglePwd;
    private ImageView ivToggleConfirmPwd;
    private CheckBox cbAgreement;
    private MaterialButton btnRegister;
    private TextView tvGoLogin;
    private TextView tvAgreement;
    private ProgressBar progressBar;
    private boolean pwdVisible = false;
    private boolean confirmPwdVisible = false;

    private String rawParentPhone = "";

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    return;
                }
                String phone = extractPhone(result.getContents());
                if (PhoneUtil.isValidPhone(phone)) {
                    rawParentPhone = phone;
                    tvParentPhone.setText(PhoneUtil.maskPhone(phone));
                } else {
                    ToastUtil.show(this, R.string.scan_qrcode_invalid);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchScanner();
                } else {
                    ToastUtil.show(this, R.string.camera_permission_required);
                }
            });

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.register));
        tvParentPhone = findViewById(R.id.tv_parent_phone);
        btnScanQrcode = findViewById(R.id.btn_scan_qrcode);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        llPhone = findViewById(R.id.ll_phone);
        llPassword = findViewById(R.id.ll_password);
        llConfirmPassword = findViewById(R.id.ll_confirm_password);
        ivTogglePwd = findViewById(R.id.iv_toggle_pwd);
        ivToggleConfirmPwd = findViewById(R.id.iv_toggle_confirm_pwd);
        cbAgreement = findViewById(R.id.cb_agreement);
        btnRegister = findViewById(R.id.btn_register);
        tvGoLogin = findViewById(R.id.tv_go_login);
        tvAgreement = findViewById(R.id.tv_agreement);
        progressBar = findViewById(R.id.progress_bar);

        rawParentPhone = getIntent().getStringExtra("parent_phone");
        if (rawParentPhone == null) {
            rawParentPhone = "";
        }
        tvParentPhone.setText(rawParentPhone.isEmpty() ? "暂无上级" : PhoneUtil.maskPhone(rawParentPhone));
        btnScanQrcode.setOnClickListener(v -> startScan());

        String prefix = getString(R.string.agreement_prefix) + getString(R.string.user_agreement) + getString(R.string.and) + getString(R.string.privacy_policy);
        tvAgreement.setText(prefix);

        ivTogglePwd.setOnClickListener(v -> {
            pwdVisible = !pwdVisible;
            etPassword.setTransformationMethod(pwdVisible ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            ivTogglePwd.setImageResource(pwdVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
            etPassword.setSelection(etPassword.length());
        });

        ivToggleConfirmPwd.setOnClickListener(v -> {
            confirmPwdVisible = !confirmPwdVisible;
            etConfirmPassword.setTransformationMethod(confirmPwdVisible ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            ivToggleConfirmPwd.setImageResource(confirmPwdVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
            etConfirmPassword.setSelection(etConfirmPassword.length());
        });

        AppConfig.loadGlobalConfig(this, apiService);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish());
        tvAgreement.setOnClickListener(v -> startActivity(new Intent(this, AgreementActivity.class)));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { updateRegisterButton(); }
        };
        etPhone.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterButton());

        etPhone.setOnFocusChangeListener((v, hasFocus) -> llPhone.setBackgroundResource(hasFocus ? R.drawable.bg_input_focus : R.drawable.bg_input_normal));
        etPassword.setOnFocusChangeListener((v, hasFocus) -> llPassword.setBackgroundResource(hasFocus ? R.drawable.bg_input_focus : R.drawable.bg_input_normal));
        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> llConfirmPassword.setBackgroundResource(hasFocus ? R.drawable.bg_input_focus : R.drawable.bg_input_normal));
    }

    private void updateRegisterButton() {
        boolean enabled = isPhoneValid() && etPassword.length() >= 6 && etConfirmPassword.length() >= 6 && cbAgreement.isChecked();
        btnRegister.setEnabled(enabled);
        btnRegister.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private boolean isPhoneValid() {
        return PhoneUtil.isValidPhone(etPhone.getText().toString().trim());
    }

    private void startScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_prompt));
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        scanLauncher.launch(options);
    }

    /**
     * 从扫码结果中提取 11 位上级手机号。
     *
     * 支持两种格式：纯手机号，或推广链接（如 http://host/batch/qrcode/scan?phone=138xxx）。
     */
    private String extractPhone(String contents) {
        if (contents == null) {
            return "";
        }
        String text = contents.trim();
        if (PhoneUtil.isValidPhone(text)) {
            return text;
        }
        Matcher matcher = Pattern.compile("1\\d{10}").matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private void attemptRegister() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!isPhoneValid()) {
            ToastUtil.show(this, R.string.phone_format_error);
            llPhone.setBackgroundResource(R.drawable.bg_input_error);
            return;
        }
        if (password.length() < 6 || password.length() > 20) {
            ToastUtil.show(this, R.string.password_length_error);
            llPassword.setBackgroundResource(R.drawable.bg_input_error);
            return;
        }
        if (!password.equals(confirmPassword)) {
            ToastUtil.show(this, R.string.password_not_match);
            llConfirmPassword.setBackgroundResource(R.drawable.bg_input_error);
            return;
        }
        if (!cbAgreement.isChecked()) {
            ToastUtil.show(this, R.string.agreement_required);
            return;
        }

        showLoading(true);
        apiService.appRegister(new AppRegisterBody(phone, password, rawParentPhone)).enqueue(new AuthApiCallback<BatchCustomerDto>() {
            @Override
            public void onSuccess(String token, BatchCustomerDto data) {
                showLoading(false);
                tokenManager.saveToken(token);
                UserSession.saveLogin(RegisterActivity.this, data);
                ToastUtil.show(RegisterActivity.this, "注册成功");
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String msg) {
                showLoading(false);
                ToastUtil.show(RegisterActivity.this, msg);
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!show);
    }
}
