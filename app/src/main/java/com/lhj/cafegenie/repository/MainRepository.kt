package com.lhj.cafegenie.repository

import com.lhj.cafegenie.ResultSearchKeyword
import com.lhj.cafegenie.remote.RemoteDataSource
import com.lhj.cafegenie.remote.RemoteDataSourceImpl
import io.reactivex.Observable
import retrofit2.Response

class MainRepository {
    val retrofitRemoteDataSource: RemoteDataSource = RemoteDataSourceImpl() //인스턴스 생성

    fun getLocationDataKakao(
        key: String,
        query: String,
        x: Double,
        y: Double,
        radius: Int
    ) : Observable<ResultSearchKeyword> = retrofitRemoteDataSource.getLocationKakao(key, query, x, y, radius)
}