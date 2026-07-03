package com.example.cj.videoeditor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.ContactAdapter;
import com.example.cj.videoeditor.bean.Contact;
import com.example.cj.videoeditor.mock.MockData;
import com.example.cj.videoeditor.utils.ToastUtil;
import java.util.List;

public class ContactActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_contact;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.contact));
        Contact online = MockData.getMockOnlineService();
        Contact headquarter = MockData.getMockHeadquarter();

        findViewById(R.id.tv_online_phone).setOnClickListener(v -> dial(online.getPhone()));
        findViewById(R.id.tv_headquarter_phone).setOnClickListener(v -> dial(headquarter.getPhone()));

        ((android.widget.TextView) findViewById(R.id.tv_online_phone)).setText(online.getPhone());
        ((android.widget.TextView) findViewById(R.id.tv_headquarter_phone)).setText(headquarter.getPhone());

        RecyclerView rvContacts = findViewById(R.id.rv_contacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        List<Contact> contacts = MockData.getMockContacts();
        rvContacts.setAdapter(new ContactAdapter(contacts));
    }

    private void dial(String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtil.show(this, "拨号失败，请检查设备权限");
        }
    }
}
