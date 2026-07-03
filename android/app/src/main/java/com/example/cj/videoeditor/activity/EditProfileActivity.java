package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.User;
import com.example.cj.videoeditor.utils.MockDataProvider;
import com.example.cj.videoeditor.utils.ToastUtil;
import com.example.cj.videoeditor.utils.UserStore;

public class EditProfileActivity extends BaseActivity {

    private ImageView ivAvatar;
    private EditText etNickname;
    private EditText etPhone;
    private Button btnSave;

    private User currentUser;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.edit_profile));
        ivAvatar = findViewById(R.id.iv_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);

        currentUser = UserStore.getUser(this);
        if (currentUser == null) {
            currentUser = MockDataProvider.getMockUser();
        }
        etNickname.setText(currentUser.getNickname());
        etPhone.setText(currentUser.getPhone());

        ivAvatar.setOnClickListener(v -> ToastUtil.show(this, "头像上传功能开发中"));
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String nickname = etNickname.getText() == null ? "" : etNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            ToastUtil.show(this, "请输入昵称");
            return;
        }
        currentUser.setNickname(nickname);
        UserStore.saveUser(this, currentUser);
        ToastUtil.show(this, "保存成功");
        finish();
    }
}
