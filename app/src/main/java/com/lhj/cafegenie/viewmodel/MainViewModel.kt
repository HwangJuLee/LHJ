package com.lhj.cafegenie.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lhj.cafegenie.ResultSearchKeyword
import com.lhj.cafegenie.repository.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel : ViewModel() {
    val mainRepository = MainRepository()

    internal val disposables = CompositeDisposable()

    val locattionResultData = MutableLiveData<ResultSearchKeyword>()
    val wifiDisconnect = MutableLiveData<Unit>()

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
                    // API를 통해 액세스 토큰을 정상적으로 받았을 때 처리할 작업을 구현
                    // 작업 중 오류가 발생하면 이 블록은 호출되지 x

                    // onResponse
                    Log.e("asdfgg", "오우야")
                    if (it.documents != null) {
                        Log.e("asdfgg", "오우야 : " + it.documents.get(0).address_name)
                        locattionResultData.value = it
                    }


                    /*}else{ //아이디 중복
                        isSuccessNetwork.value = false

                        Log.d("test",  "아이디중복: " + it.message())
                    }*/

                }) {
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등
                    // 작업이 정상적으로 완료되지 않았을 때 호출


                    // onFailure
                    Log.e("test",  "통신 실패 error : " + it.toString())
                    wifiDisconnect.value = Unit
                }
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}