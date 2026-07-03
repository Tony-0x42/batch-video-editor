package com.example.cj.videoeditor.ui.watermark;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.SimpleImageAdapter;
import com.example.cj.videoeditor.bean.ParseResult;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.MockDataProvider;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;

public class WatermarkFragment extends Fragment {

    private EditText etLink;
    private Button btnParse, btnClear, btnSave;
    private ProgressBar progressBar;
    private LinearLayout resultContainer;
    private TextView tabVideo, tabImage, tabText;
    private VideoView videoView;
    private TextView tvVideoPlaceholder;
    private RecyclerView recyclerImages;
    private ScrollView scrollText;
    private TextView tvText;
    private ParseResult result;
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watermark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etLink = view.findViewById(R.id.et_link);
        btnParse = view.findViewById(R.id.btn_parse);
        btnClear = view.findViewById(R.id.btn_clear);
        btnSave = view.findViewById(R.id.btn_save);
        progressBar = view.findViewById(R.id.progress_bar);
        resultContainer = view.findViewById(R.id.result_container);
        tabVideo = view.findViewById(R.id.tab_video);
        tabImage = view.findViewById(R.id.tab_image);
        tabText = view.findViewById(R.id.tab_text);
        videoView = view.findViewById(R.id.video_view);
        tvVideoPlaceholder = view.findViewById(R.id.tv_video_placeholder);
        recyclerImages = view.findViewById(R.id.recycler_images);
        scrollText = view.findViewById(R.id.scroll_text);
        tvText = view.findViewById(R.id.tv_text);

        btnClear.setOnClickListener(v -> etLink.setText(""));
        btnParse.setOnClickListener(v -> parse());
        btnSave.setOnClickListener(v -> {
            hideKeyboard();
            save();
        });

        tabVideo.setOnClickListener(v -> {
            hideKeyboard();
            switchTab(0);
        });
        tabImage.setOnClickListener(v -> {
            hideKeyboard();
            switchTab(1);
        });
        tabText.setOnClickListener(v -> {
            hideKeyboard();
            switchTab(2);
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            // Mock/placeholder video URL may fail; suppress system error dialog
            return true;
        });
    }

    private void parse() {
        String link = etLink.getText().toString().trim();
        if (link.isEmpty()) {
            ToastUtil.show(getContext(), R.string.parse_empty);
            return;
        }
        if (!link.startsWith("http")) {
            ToastUtil.show(getContext(), R.string.parse_format_error);
            return;
        }
        hideKeyboard();
        progressBar.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.GONE);
        btnParse.setEnabled(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            result = MockDataProvider.getParseResult();
            progressBar.setVisibility(View.GONE);
            resultContainer.setVisibility(View.VISIBLE);
            btnParse.setEnabled(true);
            showResult();
        }, 1500);
    }

    private void showResult() {
        if (result == null) return;
        recyclerImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerImages.setAdapter(new SimpleImageAdapter(result.images));
        tvText.setText(result.text);
        switchTab(0);
    }

    private void switchTab(int tab) {
        currentTab = tab;
        tabVideo.setTextColor(getResources().getColor(tab == 0 ? R.color.colorPrimary : R.color.textSecondary));
        tabImage.setTextColor(getResources().getColor(tab == 1 ? R.color.colorPrimary : R.color.textSecondary));
        tabText.setTextColor(getResources().getColor(tab == 2 ? R.color.colorPrimary : R.color.textSecondary));
        videoView.setVisibility(View.GONE);
        tvVideoPlaceholder.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        recyclerImages.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        scrollText.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        if (etLink == null) return;
        etLink.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etLink.getWindowToken(), 0);
        }
    }

    private void save() {
        if (result == null) return;
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 1000);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 356);
        if (used >= total) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.power_exhausted)
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }
        ToastUtil.show(getContext(), R.string.save_success);
    }
}
