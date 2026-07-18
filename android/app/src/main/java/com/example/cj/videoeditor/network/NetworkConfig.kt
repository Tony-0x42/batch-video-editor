package com.example.cj.videoeditor.network

import com.example.cj.videoeditor.BuildConfig

/**
 * 网络层全局常量
 *
 * 后端默认端口 8080，上下文路径为 /。
 * Android Emulator 访问本机后端请使用 10.0.2.2，真机请替换为电脑局域网 IP。
 *
 * Base URL 通过 buildConfigField 注入，可在 gradle.properties 中配置：
 * API_BASE_URL（通用）、API_BASE_URL_DEBUG / API_BASE_URL_RELEASE（按构建类型覆盖）。
 */
object NetworkConfig {

    /**
     * 后端 Base URL，末尾必须带 /
     */
    @JvmField
    val BASE_URL: String = BuildConfig.API_BASE_URL

    /**
     * 连接/读取/写入超时（秒）
     */
    const val TIMEOUT_SECONDS = 30L

    /**
     * 是否开启 OkHttp 请求/响应日志（仅 debug 包开启）
     */
    @JvmField
    val ENABLE_HTTP_LOG: Boolean = BuildConfig.DEBUG

    /**
     * 后端 JWT 认证头前缀
     */
    const val TOKEN_PREFIX = "Bearer "
}
