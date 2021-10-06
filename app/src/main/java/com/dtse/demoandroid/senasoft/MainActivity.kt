package com.dtse.demoandroid.senasoft

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.PointOfInterest
import com.huawei.hms.push.ups.entity.UPSRegisterCallBack
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.DetailSearchRequest
import com.huawei.hms.site.api.model.DetailSearchResponse
import com.huawei.hms.site.api.model.SearchStatus
import com.huawei.hms.site.api.model.Site
import java.net.URLEncoder

class MainActivity : AppCompatActivity(),HuaweiMap.OnPoiClickListener{

    private lateinit var mapView: MapView
    private var huaweiMap:HuaweiMap?=null
    private val locationPermissionContract=registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->onPermissionResult(granted)}
    private var locationService:LocationService?=null

    private fun onPermissionResult(granted: Boolean) {
        if(granted)setupLocation()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView=findViewById(R.id.mapView)
        mapView.onCreate(null)
        mapView.getMapAsync{map->onMapReady(map)}

        val locationButton=findViewById<FloatingActionButton>(R.id.locationBtn)
        locationButton.setOnClickListener{prepareLocationRequest()}

        val bannerView=findViewById<BannerView>(R.id.bannerView)
        val adParam=AdParam.Builder().build()
        bannerView.adListener=object :AdListener(){
            override fun onAdFailed(p0: Int) {
                super.onAdFailed(p0)
                val layout=findViewById<ConstraintLayout>(R.id.parentView)
                layout.removeView(bannerView)
                bannerView.destroy()
            }
        }
        bannerView.loadAd(adParam)
    }

    private fun prepareLocationRequest() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) setupLocation()
        else locationPermissionContract.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun setupLocation() {
        if(locationService==null){
            val locationService=LocationService(this)
            locationService.startRequests()
        }
    }

    private fun onMapReady(map: HuaweiMap?) {
        this.huaweiMap=map
        huaweiMap?.apply {
            setOnPoiClickListener(this@MainActivity)
            val cameraUpdate=CameraUpdateFactory.newLatLngZoom(LatLng(19.0,-99.0),15f)
            animateCamera(cameraUpdate)
        }
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

    override fun onPoiClick(poi: PointOfInterest?) {
        val resultListener: SearchResultListener<DetailSearchResponse> = object : SearchResultListener<DetailSearchResponse> {
            // Return search results upon a successful search.
            override fun onSearchResult(result: DetailSearchResponse?) {
                var site: Site? = null
                if (result == null || result.getSite().also { site = it } == null) {
                    return
                }
                Log.i("TAG", "siteId: ${site?.getSiteId()}, name: ${site?.getName()}")
                site?.let{displayInformation(it)}
            }
            // Return the result code and description upon a search exception.
            override fun onSearchError(status: SearchStatus) {
                Log.e("TAG", "Error : ${status.getErrorCode()}  ${status.getErrorMessage()}")
            }
        }

        poi?.let {
            val apiKey= URLEncoder.encode("CwEAAAAAXjK5mr7FL5C0j5512YTEDSkrD8G/mSuIxVc5m3hMgUpZr2o7zMRP8WyyytBUPU6fqabC9s4r5LDEqNYBTEPyEF5Dmz0=", "UTF-8")
            val searchService=SearchServiceFactory.create(this,apiKey)
            val request=DetailSearchRequest().apply {
                siteId=it.placeId
            }

            searchService.detailSearch(request,resultListener)
        }
    }

    private fun displayInformation(site:Site){

        AlertDialog.Builder(this)
                .setTitle(site.name)
                .setMessage(site.formatAddress)
                .setPositiveButton("ok") { dialog, _ -> dialog?.dismiss() }
                .setCancelable(false)
                .create().show()
    }



}