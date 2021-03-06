package com.lhj.cafegenie

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.*
import com.lhj.cafegenie.DB.FavoriteData
import com.lhj.cafegenie.databinding.ActivityMainBinding
import com.lhj.cafegenie.viewmodel.MainViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), OnMapReadyCallback, View.OnClickListener {

    lateinit var vm: MainViewModel

    lateinit var binding: ActivityMainBinding

    private lateinit var naverMap: NaverMap

    private var bottomCardAdapter: BottomCardAdapter? = null

    var infoWindow = InfoWindow()

    lateinit var locationSource: FusedLocationSource
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest   //현재 위치 요청

    lateinit var locationCallback: LocationCallback //현재 위치 callback

    private var currentLatitude: Double = 0.0     //현재 위도
    private var currentLongitude: Double = 0.0    //현재 경도

    private val random = Random()       //혼잡도 설정 (랜덤)

    private val locationData = ArrayList<CafeData.Place>()
    private lateinit var favoriteData : List<FavoriteData>
    private val markerData = ArrayList<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        vm =  MainViewModel(this.application)

        super.onCreate(savedInstanceState)

        //상단 레이아웃 위로 올리기
        top_layout.bringToFront()
        location_search_layout.bringToFront()

        //하단 카페 설명
        bottom_card_layout.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로
        bottom_card_layout.clipToPadding = false
        bottom_card_layout.clipChildren = false

        bottomCardAdapter = BottomCardAdapter(locationData)
        bottom_card_layout.adapter = bottomCardAdapter

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
        map_fragment.getMapAsync(this)

//        val fm = supportFragmentManager
//        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
//            ?: MapFragment.newInstance(NaverMapOptions().zoomControlEnabled(false))
//                .also {
//                    fm.beginTransaction().add(R.id.map_fragment, it).commit()
//                }
//        mapFragment.getMapAsync {
//            val zoomControlView = findViewById(R.id.zoom) as ZoomControlView
//            zoomControlView.map = naverMap
//        }

        location_search_layout.setOnClickListener(this)
    }

    override fun onMapReady(p0: NaverMap) {

        naverMap = p0

        initLocation()

        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
        naverMap.isIndoorEnabled = true
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.None


        val uiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = true
        uiSettings.isScaleBarEnabled = true
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isRotateGesturesEnabled = true

        naverMap.onMapClickListener = NaverMap.OnMapClickListener { pointF, latLng ->
            if (infoWindow != null) {
                infoWindow.close()
            }
        }

        naverMap.setOnSymbolClickListener { symbol ->
            if (symbol.caption == "서울특별시청") {
//                Toast.makeText(this, "서울시청 클릭", Toast.LENGTH_SHORT).show()
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
            } else {
                // 이벤트 전파, OnMapClick 이벤트가 발생함
            }
            true
        }

        //카메라 움직임 감지 리스너
        naverMap.addOnCameraIdleListener {
            location_search_layout.visibility = View.VISIBLE
            currentLatitude = naverMap.cameraPosition.target.latitude
            currentLongitude = naverMap.cameraPosition.target.longitude
        }
//        CameraUpdate.scrollAndZoomTo(LatLng(100.5666102, 126.9783881), 15.0);

    }

    override fun observeViewModel() {
        addObservableCafeData()
        getFavorites()
    }

    override fun initViewBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        map_fragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        map_fragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_fragment.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStop() {
        super.onStop()
        map_fragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_fragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_fragment.onLowMemory()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.location_search_layout ->

                vm.viewCommunicate(
                    KAKAO_API_KEY,
                    "cafe",
                    currentLongitude,
                    currentLatitude,
                    10000
                )
        }
    }

    private fun addObservableCafeData() {
        vm.locationResultData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {

                if (locationData.size > 0) {
                    locationData.clear()
                }

                if (markerData.size > 0) {
                    for (i in markerData.indices) {
                        markerData.get(i).map = null
                    }
                    markerData.clear()
                }

                for (i in it.documents.indices) {
                    //혼잡도 세팅 (랜덤)
                    it.documents.get(i).congestion = random.nextInt(100);

                    var marker = Marker()
                    marker.position = LatLng(
                        it.documents.get(i).y.toDouble(),
                        it.documents.get(i).x.toDouble()
                    )
                    marker.width = 100
                    marker.height = 100
//                    marker.captionText = result?.get(i)?.place_name
//                    marker.icon = OverlayImage.fromResource(R.drawable.cafe_img2)
                    marker.icon = MarkerIcons.BLACK
                    if (it.documents.get(i).congestion <= 33) {
                        marker.icon = OverlayImage.fromResource(R.drawable.ic_coffee_green)
                    } else if (it.documents.get(i).congestion > 33 && it.documents.get(i).congestion <= 66) {
                        marker.icon = OverlayImage.fromResource(R.drawable.ic_coffee_yellow)
                    } else {
                        marker.icon = OverlayImage.fromResource(R.drawable.ic_coffee_red)
                    }
                    marker.setCaptionAligns(Align.Top)

                    marker.onClickListener = Overlay.OnClickListener { overlay ->
                        val marker = overlay as Marker

                        //infowindow 커스터 마이징
                        infoWindow.adapter =
                            object : InfoWindow.DefaultViewAdapter(this@MainActivity) {
                                override fun getContentView(p0: InfoWindow): View {

                                    var view = View.inflate(
                                        this@MainActivity,
                                        R.layout.location_item,
                                        null
                                    )

                                    var congestion_iv: ImageView =
                                        view.findViewById(R.id.congestion_iv)
                                    var congestion_tv: TextView =
                                        view.findViewById(R.id.congestion_tv)
                                    var cafeTv: TextView = view.findViewById(R.id.cafe_tv)

                                    if (it.documents.get(i).congestion <= 33) {
                                        congestion_iv.setImageResource(R.drawable.ic_coffee_green)
                                    } else if (it.documents.get(i).congestion > 33 && it.documents.get(i).congestion <= 66) {
                                        congestion_iv.setImageResource(R.drawable.ic_coffee_yellow)
                                    } else {
                                        congestion_iv.setImageResource(R.drawable.ic_coffee_red)
                                    }

//                                congestion_iv.setImageDrawable()
                                    congestion_tv.text = locationData.get(i).congestion.toString() + "${'%'}"
                                    cafeTv.text = locationData.get(i).place_name

                                    return view
                                }
                            }

                        infoWindow.onClickListener = Overlay.OnClickListener { overlay ->
                            val intent = Intent(this@MainActivity, InfoActivity::class.java)
                            intent.putExtra("cafeData", locationData.get(i));
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
                    Log.e("asdfgg" , " it.documents.toString() : " + it.documents.toString())
                    locationData.add(it.documents.get(i))
                    markerData.add(marker)
                }

                for (i in markerData.indices) {
                    markerData.get(i).map = naverMap
                }

//                bottomCardAdapter = BottomCardAdapter(locationData)
//                bottom_card_layout.adapter = bottomCardAdapter
                bottomCardAdapter?.setData(locationData)
                bottomCardAdapter!!.setOnItemClickListener(object : BottomCardAdapter.ItemClick {
                    override fun onItemClick(v: View?, position: Int) {
                        val intent = Intent(this@MainActivity, InfoActivity::class.java)
                        intent.putExtra("cafeData", locationData?.get(position))
                        startActivity(intent)
                    }
                })
                bottomCardAdapter!!.setOnFavoriteClickListener(object : BottomCardAdapter.ItemClick {
                    override fun onItemClick(v: View?, position: Int) {
                        //즐겨찾기 시 처리

                    }
                })

            }
        })
    }

    private fun getFavorites(){
        vm.getAll().observe(this, androidx.lifecycle.Observer {
            if(it != null){
                favoriteData = it

            }
        })
    }

    private fun addFavorites(){

    }

    private fun removeFavorites(){
        vm.getAll().observe(this, androidx.lifecycle.Observer {
            if(it != null){

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

//                        searchTask("카페", location.longitude, location.latitude);
                        vm.viewCommunicate(
                            KAKAO_API_KEY,
                            "cafe",
                            location.longitude,
                            location.latitude,
                            10000
                        )

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