package com.lhj.cafegenie.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lhj.cafegenie.CafeData
import com.lhj.cafegenie.DB.FavoriteData
import com.lhj.cafegenie.repository.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(application : Application) : ViewModel() {
    private val mainRepository = MainRepository(application)
    private val favorites = mainRepository.getAllFav()
    private val disposables = CompositeDisposable()
    val locationResultData = MutableLiveData<CafeData.ResultSearchKeyword>()

    fun viewCommunicate(
        key: String,
        query: String,
        x: Double,
        y: Double,
        radius: Int
    ) {
        disposables.add(
            mainRepository.getLocationDataKakao(key, query, x, y, radius)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                // 스트림이 종료될 때 수행할 작업을 구현
                .doOnTerminate {}
                // 옵서버블을 구독
                .subscribe({
                    // onResponse
                    if (it.documents != null) {
                        locationResultData.value = it
                    }
                }) {
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등
                    // 작업이 정상적으로 완료되지 않았을 때 호출

                    // onFailure
                    Log.e("test", "통신 실패 error : $it")
                }
        )
    }

    fun getAll(): LiveData<List<FavoriteData>> {
        return this.favorites
    }

    fun insert(favData : FavoriteData) {
        mainRepository.insertFav(favData)
    }

    fun delete(favData : FavoriteData) {
        mainRepository.deleteFav(favData)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}