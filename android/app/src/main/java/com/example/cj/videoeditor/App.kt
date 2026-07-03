package com.example.cj.videoeditor

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp

/**
 * 视频编辑器 Application 入口
 *
 * 继承自原有 MyApplication，保留旧代码的上下文初始化逻辑，
 * 同时启用 Hilt 依赖注入。
 */
@HiltAndroidApp
class VideoEditorApp : MyApplication() {

    override fun onCreate() {
        super.onCreate()
        initCoil(this)
    }

    private fun initCoil(application: Application) {
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .components {
                    add(VideoFrameDecoder.Factory())
                }
                .build()
        }
    }
}
