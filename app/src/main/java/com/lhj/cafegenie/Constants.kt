package com.lhj.cafegenie

import android.Manifest

const val PERMISSIONS_REQUEST_CODE = 1000
const val LOCATION_PERMISSION_REQUEST_CODE = 1001
const val KAKAO_API_KEY = "KakaoAK b2251f8a2e1755603e4c5bfd9edaa2cc"

val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)