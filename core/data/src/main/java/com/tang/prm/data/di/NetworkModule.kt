package com.tang.prm.data.di

import com.tang.prm.data.remote.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        retryInterceptor: RetryInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(retryInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)   // SSE 流最长等待 120 秒
        .writeTimeout(600, TimeUnit.SECONDS)  // 10 分钟，大文件上传需要
        .build()
}
