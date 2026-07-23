package com.tang.prm.data.di

import com.tang.prm.data.remote.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * AI 流式请求专用 OkHttpClient。
     * - 不附加 RetryInterceptor：AI POST 请求不应自动重试，由 AiRepositoryImpl 自行决定重试逻辑
     * - readTimeout 加长至 5 分钟：SSE 长对话可能持续较久
     */
    @Provides
    @Singleton
    @Named("ai")
    fun provideAiOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * WebDav 大文件传输专用 OkHttpClient。
     * - 附加 RetryInterceptor：网络抖动时自动重试（仅对幂等读操作 PROPFIND/GET 生效，
     *   写操作 PUT/POST/DELETE/PATCH 由 RetryInterceptor.NON_RETRYABLE_METHODS 放行不重试，
     *   避免重复上传/删除等副作用）
     * - writeTimeout 加长至 10 分钟：db.zip 等大文件上传在慢网下需要
     */
    @Provides
    @Singleton
    @Named("webdav")
    fun provideWebDavOkHttpClient(
        retryInterceptor: RetryInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(retryInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(600, TimeUnit.SECONDS)
        .build()
}
