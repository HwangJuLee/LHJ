package com.lhj.cafecheck

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
}