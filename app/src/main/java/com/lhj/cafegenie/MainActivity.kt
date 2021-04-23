package com.lhj.cafegenie

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val BASE_URL_NAVER_API = "https://openapi.naver.com"
    }

    var context: Context? = null
    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    lateinit var naverMap: NaverMap
    var marker = Marker()

    var latitude : Double = 0.0
    var longitude : Double = 0.0

    var locatioNManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        mapView = findViewById(R.id.map_fragment);

        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("6cwm0r1vwh");

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapView.getMapAsync(this)

    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0

        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
        naverMap.isIndoorEnabled = true
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.None

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isRotateGesturesEnabled = true

        naverMap.setOnMapClickListener(NaverMap.OnMapClickListener { pointF, latLng ->
            if (marker != null) {
                marker.map = null

            }
        })

        naverMap.setOnSymbolClickListener { symbol ->
            if (symbol.caption == "서울특별시청") {
                Toast.makeText(this, "서울시청 클릭", Toast.LENGTH_SHORT).show()
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            } else {
                // 이벤트 전파, OnMapClick 이벤트가 발생함
                Toast.makeText(this, "symbol : " + symbol.caption, Toast.LENGTH_SHORT).show()
                marker.position = LatLng(symbol.position.latitude, symbol.position.longitude)
                marker.map = naverMap
            }
            true
        }

        CameraUpdate.scrollAndZoomTo(LatLng(27.5666102, 126.9783881), 15.0);

        searchTask();
        getLocation();

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun searchTask() {
        val clientId = "9m_BiCP4gNJKZy9bPC23" //애플리케이션 클라이언트 아이디값"
        val clientSecret = "x_YxnhMj0V" //애플리케이션 클라이언트 시크릿값"

        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_NAVER_API)
            .addConverterFactory(GsonConverterFactory.create()).build()

        val api = retrofit.create(NaverAPI::class.java)
        val callgetSearchLocation = api.getSearchLocation(clientId, clientSecret, "여의도 카페", 10)

        callgetSearchLocation.enqueue(object : Callback<ResultData> {
            override fun onResponse(
                call: Call<ResultData>,
                response: Response<ResultData>
            ) {
                Log.d("결과", "성공 : ${response.raw()}")
                Log.d("결과", "성공 : ${response.body()}")
            }

            override fun onFailure(call: Call<ResultData>, t: Throwable) {
                Log.d("결과:", "실패 : $t")
            }
        })
    }

    private fun getLocation() {
        locatioNManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var userLocation: Location = getLatLng()
        if (userLocation != null) {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
            Log.d("CheckCurrentLocation", "현재 내 위치 값: ${latitude}, ${longitude}")

            var mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
            var mResultList: List<Address>? = null
            try {
                mResultList = mGeoCoder.getFromLocation(
                    latitude!!, longitude!!, 1
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mResultList != null) {
                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
            }
        }
    }

    private fun getLatLng(): Location {
        var currentLatLng: Location? = null
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("asdfgg" , "권한은 있어");
            val locatioNProvider = LocationManager.GPS_PROVIDER
            currentLatLng = locatioNManager?.getLastKnownLocation(locatioNProvider)
            Log.e("asdfgg" , "currentLatLng : " + currentLatLng?.latitude);
        } else {
            Log.e("asdfgg" , "권한은 없어");
            currentLatLng = getLatLng()
        }
        return currentLatLng!!
    }

//    private fun checkPermissions() {
//        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
//        var rejectedPermissionList = ArrayList<String>()
//
//        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
//        for(permission in REQUIRED_PERMISSIONS){
//            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                //만약 권한이 없다면 rejectedPermissionList에 추가
//                rejectedPermissionList.add(permission)
//            }
//        }
//        //거절된 퍼미션이 있다면...
//        if(rejectedPermissionList.isNotEmpty()){
//            //권한 요청!
//            val array = arrayOfNulls<String>(rejectedPermissionList.size)
//            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), PERMISSIONS_REQUEST_CODE)
//        }
//    }
}