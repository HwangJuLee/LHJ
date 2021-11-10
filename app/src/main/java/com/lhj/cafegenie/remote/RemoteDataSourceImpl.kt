package com.lhj.cafegenie.remote

import com.lhj.cafegenie.retrofit.SearchServiceImpl

class RemoteDataSourceImpl : RemoteDataSource {

    private val api = SearchServiceImpl.service

    override fun getLocationKakao(
        key: String,
        query: String,
        x: Double,
        y: Double,
        radius: Int
    ) = api.getSearchLocationKakao(key, query, x, y, radius).map{it}
}