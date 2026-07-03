package com.example.cj.videoeditor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cj.videoeditor.MainActivity;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.mock.MockData;
import com.example.cj.videoeditor.mock.UserSession;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends BaseActivity {

    private TextView tvParentPhone;
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
    private boolean pwdVisible = false;
    private boolean confirmPwdVisible = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.register));
        tvParentPhone = findViewById(R.id.tv_parent_phone);
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

        String parent = getIntent().getStringExtra("parent_phone");
        tvParentPhone.setText(parent == null || parent.isEmpty() ? "暂无上级" : parent.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));

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
        String phone = etPhone.getText().toString().trim();
        return phone.length() == 11 && phone.startsWith("1");
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

        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.loading);
        btnRegister.postDelayed(() -> {
            UserSession.saveLogin(this, MockData.getMockUser());
            ToastUtil.show(this, "注册成功");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1000);
    }
}
