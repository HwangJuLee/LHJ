package com.lhj.cafegenie

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val BASE_URL_KAKAO_API = "https://dapi.kakao.com"
        const val KAKAO_API_KEY = "KakaoAK b2251f8a2e1755603e4c5bfd9edaa2cc"
    }

    private lateinit var mapView: MapView
    lateinit var naverMap: NaverMap
    lateinit var top_layout: LinearLayout
    lateinit var location_search_layout: LinearLayout
    lateinit var bottom_card_layout: ViewPager2

    var bottom_card_adapter : BottomCardAdapter? = null

    //    var marker = Marker()
    var infoWindow = InfoWindow()

    lateinit var locationSource: FusedLocationSource
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest   //현재 위치 요청
    var priority: Int = 0
    var interval: Int = 0
    lateinit var locationCallback: LocationCallback //현재 위치 callback

    var current_latitude: Double = 0.0     //현재 위도
    var current_longitude: Double = 0.0    //현재 경도

    val random = Random()       //혼잡도 설정 (랜덤)

    val location_data  = ArrayList<Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val pageMarginPx = 15
//        val pagerWidth = 300
//        val screenWidth = resources.displayMetrics.widthPixels
//        val offsetPx = screenWidth - pageMarginPx - pagerWidth

        //위치 권한 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "위치 권한 요청에 동의해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        mapView = findViewById(R.id.map_fragment)
        top_layout = findViewById(R.id.top_layout)
        location_search_layout = findViewById(R.id.location_search_layout)
        bottom_card_layout = findViewById(R.id.bottom_card_layout)

        //상단 레이아웃 위로 올리기
        top_layout.bringToFront()
        location_search_layout.bringToFront()

        //하단 카페 설명
        bottom_card_adapter = BottomCardAdapter(location_data);
        bottom_card_layout.adapter = bottom_card_adapter
        bottom_card_layout.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로
//        bottom_card_layout.setPageTransformer { page, position ->
//            page.translationX = position * -offsetPx
//        }
//        bottom_card_adapter.

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60 * 1000
        }

        //현재 위치 받기
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for ((i, location) in it.locations.withIndex()) {
                        Log.d("asdfgg", "#$i ${location.latitude} , ${location.longitude}")
                    }
                }
            }
        }

        //네이버 지도 init
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("6cwm0r1vwh");
        mapView.getMapAsync(this)

    }

    override fun onMapReady(p0: NaverMap) {
        initLocation()

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
//            if (marker != null) {
//                marker.map = null
//            }
            if (infoWindow != null) {
                infoWindow.close()
            }
        })

        naverMap.setOnSymbolClickListener { symbol ->
            if (symbol.caption == "서울특별시청") {
//                Toast.makeText(this, "서울시청 클릭", Toast.LENGTH_SHORT).show()
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            } else {
                // 이벤트 전파, OnMapClick 이벤트가 발생함
//                Toast.makeText(this, "symbol : " + symbol.caption, Toast.LENGTH_SHORT).show()
//                marker.position = LatLng(symbol.position.latitude, symbol.position.longitude)
//                marker.map = naverMap
            }
            true
        }

        naverMap.addOnCameraChangeListener { reason, animated ->

        }
        naverMap.addOnCameraIdleListener {
            Log.e(
                "asdfgg",
                "카메라 움직임 종료 : " + naverMap.cameraPosition.target.latitude + "    " + naverMap.cameraPosition.target.longitude

            )

            location_search_layout.visibility = View.VISIBLE

            current_latitude = naverMap.cameraPosition.target.latitude
            current_longitude = naverMap.cameraPosition.target.longitude
        }
//        CameraUpdate.scrollAndZoomTo(LatLng(100.5666102, 126.9783881), 15.0);

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
        fusedLocationClient.removeLocationUpdates(locationCallback)
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

    fun searchTask(query: String, latitude: Double, longitude: Double) {

        var gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_KAKAO_API)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()

        val api = retrofit.create(SearchAPI::class.java)
        val callgetSearchLocation =
            api.getSearchLocationKakao(KAKAO_API_KEY, "cafe", latitude, longitude, 10000)
//        val callgetSearchLocation = api.testtt("pcweb","카페","all","127.11111;37.44444",1,20,true,"ko")

        callgetSearchLocation.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                Log.d("결과", "성공 : ${response.raw()}")
                Log.d("결과", "성공 : ${response.body()}")
                Log.d("결과", "성공 : ${response.body()?.documents?.size}")
                var result = response.body()?.documents
                for (i in response.body()?.documents?.indices!!) {
                    //혼잡도 세팅 (랜덤)
                    result?.get(i)?.congestion = random.nextInt(100);

                    var marker = Marker()
                    marker.position = LatLng(
                        result?.get(i)?.y?.toDouble()!!,
                        result?.get(i)?.x?.toDouble()!!
                    )
                    marker.width = 70
                    marker.height = 90
//                    marker.captionText = result?.get(i)?.place_name
//                    marker.icon = OverlayImage.fromResource(R.drawable.cafe_img2)
                    marker.icon = MarkerIcons.BLACK
                    Log.e("asdfgg", "result?.get(i)?.congestion : " + result?.get(i)?.congestion);
                    if (result?.get(i)?.congestion <= 33) {
                        marker.iconTintColor = Color.GREEN
                    } else if (result?.get(i)?.congestion > 33 && result?.get(i)?.congestion <= 66) {
                        marker.iconTintColor = Color.YELLOW
                    } else {
                        marker.iconTintColor = Color.RED
                    }
                    marker.setCaptionAligns(Align.Top)

                    marker.onClickListener = Overlay.OnClickListener { overlay ->
                        val marker = overlay as Marker

//                        infoWindow.adapter =
//                            object : InfoWindow.DefaultTextAdapter(this@MainActivity) {
//                                override fun getText(infoWindow: InfoWindow): CharSequence {
//                                    return result?.get(i)?.place_name + "\n" + "혼잡도 : " + result?.get(
//                                        i
//                                    )?.congestion + "%"
//                                }
//                            }
                        infoWindow.adapter = object : InfoWindow.DefaultViewAdapter(this@MainActivity){
                            override fun getContentView(p0: InfoWindow): View {

                                var view = View.inflate(this@MainActivity, R.layout.location_item, null)
                                var congestion_iv : ImageView = view.findViewById(R.id.congestion_iv)
                                var cafe_tv : TextView = view.findViewById(R.id.cafe_tv)

//                                congestion_iv.setImageDrawable()
                                cafe_tv.setText(result?.get(i).place_name)

                                return view
                            }
                        }

                        infoWindow.onClickListener = Overlay.OnClickListener { overlay ->

                            Log.e("asdfgg", "정보창 클릭 : " + result?.get(i).place_name);

                            val intent = Intent(this@MainActivity, InfoActivity::class.java)
                            intent.putExtra("cafe_data", result?.get(i));
                            startActivity(intent)

                            true
                        }

                        if (marker.infoWindow == null) {
                            // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                            infoWindow.open(marker)
                        } else {
                            // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                            infoWindow.close()
                        }

                        true
                    }

                    marker.map = naverMap
                    location_data.add(result?.get(i))
                    bottom_card_layout.adapter?.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                Log.d("결과:", "실패 : $t")
            }
        })
    }

    private fun initLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    Log.e("asdfgg", "location get fail")
                } else {
                    Log.e("asdfgg", "${location.latitude} , ${location.longitude}")

                    if (naverMap != null) {
                        val cameraUpdate =
                            CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
                        naverMap.moveCamera(cameraUpdate)
                        fusedLocationClient.removeLocationUpdates(locationCallback)

                        searchTask("카페", location.longitude, location.latitude);

                    }
                }
            }
            .addOnFailureListener {

            }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
}