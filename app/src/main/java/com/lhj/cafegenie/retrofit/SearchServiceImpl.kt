package com.lhj.cafegenie.retrofit

import com.lhj.cafegenie.MainActivity
import com.lhj.cafegenie.Utils.CookiesIntercepter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object SearchServiceImpl {
    private const val BASE_URL = "https://dapi.kakao.com"

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(CookiesIntercepter())
            .addNetworkInterceptor(CookiesIntercepter()).build()

    private val retrofit: Retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).client(
            okHttpClient
        )
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    val service: SearchService = retrofit.create(
        SearchService::class.java
    )
}