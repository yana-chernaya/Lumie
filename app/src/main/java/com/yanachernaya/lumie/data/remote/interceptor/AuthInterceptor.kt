package com.yanachernaya.lumie.data.remote.interceptor

import com.yanachernaya.lumie.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.SERVER_BEARER_TOKEN}")
            .build()

        return chain.proceed(request)
    }
}