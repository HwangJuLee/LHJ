package com.lhj.cafegenie.retrofit

import com.lhj.cafegenie.ResultData
import com.lhj.cafegenie.ResultSearchKeyword
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchService {
    //네이버 위치 검색 API
    @GET("v1/search/local.json")
    fun getSearchLocationNaver(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int? = null,
        @Query("start") start: Int? = null,
        @Query("sort") sort: String? = null
    ): Call<ResultData>

//    //카카오 위치 검색 API
//    @GET("v2/local/search/keyword.json")
//    fun getSearchLocationKakao(
//        @Header("Authorization") key: String,     // 카카오 API 인증키 [필수]
//        @Query("query") query: String,             // 검색을 원하는 질의어 [필수]
//        @Query("x") x: Double,             // latitude
//        @Query("y") y: Double,            // longitude
//        @Query("radius") radius: Int             // radius
//    ): Call<ResultSearchKeyword>

    //카카오 위치 검색 API
    @GET("v2/local/search/keyword.json")
    fun getSearchLocationKakao(
        @Header("Authorization") key: String,     // 카카오 API 인증키 [필수]
        @Query("query") query: String,             // 검색을 원하는 질의어 [필수]
        @Query("x") x: Double,             // latitude
        @Query("y") y: Double,            // longitude
        @Query("radius") radius: Int             // radius
    ): Observable<ResultSearchKeyword>
}