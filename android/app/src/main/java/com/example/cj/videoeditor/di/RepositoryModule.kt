package com.example.cj.videoeditor.di

import com.example.cj.videoeditor.data.repository.DraftRepositoryImpl
import com.example.cj.videoeditor.data.repository.TimelineRepositoryImpl
import com.example.cj.videoeditor.domain.repository.DraftRepository
import com.example.cj.videoeditor.domain.repository.TimelineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据仓库绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(
        impl: TimelineRepositoryImpl
    ): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindDraftRepository(
        impl: DraftRepositoryImpl
    ): DraftRepository
}
