package com.lhj.cafegenie

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.naver.maps.map.LocationTrackingMode
import java.util.*
import kotlin.concurrent.timer

class Intro : AppCompatActivity() {
    private var time = 0
    private var timerTask : Timer? = null
    public var con : Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        con = this

        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(timerTask != null){
            timerTask?.cancel()
        }
    }

    private fun startTimer(){
        timerTask = timer(period = 1000){
            time++

            val sec = time

            runOnUiThread {
                Log.e("asdfgg" , "타이머 : " + sec)
                if (sec == 2) {
                    val nextIntent = Intent(con, MainActivity::class.java)
                    startActivity(nextIntent)
                    finish()
                }
            }
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
//                grantResults)) {
//            if (!locationSource.isActivated) { // 권한 거부됨
//                naverMap.locationTrackingMode = LocationTrackingMode.None
//            }
//            return
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
}