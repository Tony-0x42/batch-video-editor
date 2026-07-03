package com.example.cj.videoeditor.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.mock.MockData;

public class AgreementActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_agreement;
    }

    @Override
    protected void initViews() {
        setTitle(getString(R.string.user_agreement));
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });
        webView.loadDataWithBaseURL(null, wrapHtml(MockData.getUserAgreement()), "text/html", "utf-8", null);
    }

    private String wrapHtml(String body) {
        return "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{padding:16px;line-height:1.6;color:#333;}h2{color:#2196F3;}</style></head><body>" + body + "</body></html>";
    }
}
