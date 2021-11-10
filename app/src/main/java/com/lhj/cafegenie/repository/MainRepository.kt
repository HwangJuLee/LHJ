package com.lhj.cafegenie.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.lhj.cafegenie.CafeData
import com.lhj.cafegenie.DB.FavoriteDB
import com.lhj.cafegenie.DB.FavoriteDao
import com.lhj.cafegenie.DB.FavoriteData
import com.lhj.cafegenie.remote.RemoteDataSource
import com.lhj.cafegenie.remote.RemoteDataSourceImpl
import io.reactivex.Observable

class MainRepository(application: Application) {
    private val favDatabase = FavoriteDB.getInstance(application)!!
    private val favDao: FavoriteDao = favDatabase.favDao()
    private val favorites: LiveData<List<FavoriteData>> = favDao.getAllFav()

    private val retrofitRemoteDataSource: RemoteDataSource =
        RemoteDataSourceImpl() //retrofit2 인스턴스 생성

    fun getLocationDataKakao(
        key: String,
        query: String,
        x: Double,
        y: Double,
        radius: Int
    ): Observable<CafeData.ResultSearchKeyword> =
        retrofitRemoteDataSource.getLocationKakao(key, query, x, y, radius)

    fun getAllFav(): LiveData<List<FavoriteData>> {
        return favorites
    }

    fun insertFav(favData: FavoriteData) {
        try {
            val thread = Thread(Runnable {
                favDao.insertFav(favData)
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFav(favData: FavoriteData) {
        try {
            val thread = Thread(Runnable {
                favDao.deleteFav(favData)
            })
            thread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}