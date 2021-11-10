package com.lhj.cafegenie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*

class InfoActivity : BaseActivity() {

    lateinit var cafeData : CafeData.Place
    lateinit var webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        cafeData = intent.getSerializableExtra("cafeData") as CafeData.Place

        webview = findViewById(R.id.webview)

        webview.settings.javaScriptEnabled = true // 자바 스크립트 허용

        // 웹뷰안에 새 창이 뜨지 않도록 방지
        webview.webViewClient = WebViewClient()
        webview.webChromeClient = WebChromeClient()

        // 원하는 주소를 WebView에 연결
        webview.loadUrl(cafeData.place_url.replace("http", "https"))
    }

    override fun observeViewModel() {

    }

    override fun initViewBinding() {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if(webview.canGoBack()){
            webview.goBack() // 이전 페이지로 갈 수 있다면 이동하고
        } else {
            super.onBackPressed() // 더 이상 이전 페이지가 없을 때 앱이 종료된다.
        }
    }
}