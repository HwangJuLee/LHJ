package com.lhj.cafegenie.remote

import com.lhj.cafegenie.ResultSearchKeyword
import io.reactivex.Observable

interface RemoteDataSource {
    fun getLocationKakao(
        key: String,
        query: String,
        x: Double,
        y: Double,
        radius: Int
    ) : Observable<ResultSearchKeyword>
}