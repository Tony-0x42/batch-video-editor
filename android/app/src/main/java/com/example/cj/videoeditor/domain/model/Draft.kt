package com.example.cj.videoeditor.domain.model

/**
 * 草稿领域模型
 */
data class Draft(
    val id: String,
    val title: String,
    val thumbnailColorHex: String,
    val durationUs: Long,
    val updatedAtMillis: Long
)
