package com.example.cj.videoeditor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cj.videoeditor.AppConfig;
import com.example.cj.videoeditor.MainActivity;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.model.User;
import com.example.cj.videoeditor.utils.MockData;
import com.example.cj.videoeditor.utils.PhoneUtil;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private CheckBox cbAgreement;
    private Button btnLogin;
    private TextView tvAgreement, tvPrivacy, tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        cbAgreement = findViewById(R.id.cb_agreement);
        btnLogin = findViewById(R.id.btn_login);
        tvAgreement = findViewById(R.id.tv_user_agreement);
        tvPrivacy = findViewById(R.id.tv_privacy);
        tvGoRegister = findViewById(R.id.tv_go_register);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButton();
            }
        };
        etPhone.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> updateLoginButton());

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvAgreement.setOnClickListener(v -> startActivity(new Intent(this, AgreementActivity.class)));
        tvPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacyActivity.class)));
        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void updateLoginButton() {
        boolean enabled = PhoneUtil.isValidPhone(etPhone.getText().toString().trim())
                && etPassword.getText().toString().trim().length() >= 6
                && cbAgreement.isChecked();
        btnLogin.setEnabled(enabled);
        btnLogin.setBackgroundResource(enabled ? R.drawable.bg_button_blue : R.drawable.bg_button_disabled);
    }

    private void attemptLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (!PhoneUtil.isValidPhone(phone)) {
            ToastUtil.show(this, R.string.phone_format_error);
            return;
        }
        if (password.length() < 6 || password.length() > 20) {
            ToastUtil.show(this, R.string.password_length_error);
            return;
        }
        if (!cbAgreement.isChecked()) {
            ToastUtil.show(this, R.string.agreement_required);
            return;
        }
        simulateLogin(phone);
    }

    private void simulateLogin(String phone) {
        btnLogin.setEnabled(false);
        btnLogin.postDelayed(() -> {
            User user = MockData.getUser();
            SharedPrefUtil.putBoolean(this, AppConfig.SP_KEY_IS_LOGIN, true);
            SharedPrefUtil.putString(this, AppConfig.SP_KEY_USER_PHONE, phone);
            SharedPrefUtil.putString(this, AppConfig.SP_KEY_USER_NAME, user.name);
            SharedPrefUtil.putString(this, AppConfig.SP_KEY_USER_AVATAR, user.avatar);
            SharedPrefUtil.putBoolean(this, AppConfig.SP_KEY_USER_VIP, user.vip);
            SharedPrefUtil.putString(this, AppConfig.SP_KEY_USER_VIP_EXPIRE, user.vipExpire);
            SharedPrefUtil.putInt(this, AppConfig.SP_KEY_COMPUTE_TOTAL, 1000);
            SharedPrefUtil.putInt(this, AppConfig.SP_KEY_COMPUTE_USED, 356);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 800);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("确定退出应用？")
                .setPositiveButton(R.string.confirm, (dialog, which) -> finish())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
