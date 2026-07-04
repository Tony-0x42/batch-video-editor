package com.example.cj.videoeditor.ui.watermark;

import android.content.Context;
import android.os.Bundle;
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
import com.example.cj.videoeditor.network.ApiCallback;
import com.example.cj.videoeditor.network.ApiService;
import com.example.cj.videoeditor.network.dto.ComputingConsumeBody;
import com.example.cj.videoeditor.network.dto.ComputingConsumeDto;
import com.example.cj.videoeditor.network.dto.WatermarkParseBody;
import com.example.cj.videoeditor.network.dto.WatermarkParseDto;
import com.example.cj.videoeditor.utils.AppConfig;
import com.example.cj.videoeditor.utils.SharedPrefUtil;
import com.example.cj.videoeditor.utils.ToastUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WatermarkFragment extends Fragment {

    @Inject
    ApiService apiService;

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
            // 视频 URL 可能无法直接播放，屏蔽系统错误弹窗
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

        apiService.parseWatermark(new WatermarkParseBody(link)).enqueue(new ApiCallback<WatermarkParseDto>() {
            @Override
            public void onSuccess(WatermarkParseDto data) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                btnParse.setEnabled(true);
                if (data == null) {
                    ToastUtil.show(getContext(), AppConfig.getParseFailTip(requireContext()));
                    return;
                }
                result = toParseResult(data);
                resultContainer.setVisibility(View.VISIBLE);
                showResult();
            }

            @Override
            public void onError(String msg) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                btnParse.setEnabled(true);
                ToastUtil.show(getContext(), msg != null ? msg : AppConfig.getParseFailTip(requireContext()));
            }
        });
    }

    private ParseResult toParseResult(WatermarkParseDto dto) {
        ParseResult parseResult = new ParseResult();
        parseResult.videoUrl = dto.getVideoUrl();
        parseResult.images = dto.getImageList();
        parseResult.text = dto.getVideoText();
        return parseResult;
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
        int total = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_TOTAL, 0);
        int used = SharedPrefUtil.getInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, 0);
        if (used >= total) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(AppConfig.getPowerExhaustedMessage(requireContext()))
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            return;
        }

        btnSave.setEnabled(false);
        apiService.consumeComputingPower(new ComputingConsumeBody(2, 1.0, "watermark_download"))
                .enqueue(new ApiCallback<ComputingConsumeDto>() {
                    @Override
                    public void onSuccess(ComputingConsumeDto data) {
                        if (!isAdded() || getContext() == null) {
                            return;
                        }
                        btnSave.setEnabled(true);
                        if (data != null && data.getRemain() != null) {
                            SharedPrefUtil.putInt(requireContext(), AppConfig.SP_KEY_COMPUTE_USED, total - data.getRemain().intValue());
                        }
                        ToastUtil.show(getContext(), R.string.save_success);
                    }

                    @Override
                    public void onError(String msg) {
                        if (!isAdded() || getContext() == null) {
                            return;
                        }
                        btnSave.setEnabled(true);
                        new AlertDialog.Builder(requireContext())
                                .setMessage(msg != null ? msg : AppConfig.getPowerExhaustedMessage(requireContext()))
                                .setPositiveButton(R.string.confirm, null)
                                .show();
                    }
                });
    }
}
