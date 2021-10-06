package com.dtse.demoandroid.senasoft

import android.content.Context
import android.content.IntentSender
import android.os.Looper
import android.util.Log
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.huawei.hms.maps.LocationSource

class LocationService(val context:Context): LocationCallback() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // Define a location request object.




    fun startRequests(){
        val settingsClient = LocationServices.getSettingsClient(context)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval=1000
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
// Check the device location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
            // Define the listener for success in calling the API for checking device location settings.
            .addOnSuccessListener(OnSuccessListener { locationSettingsResponse ->
                fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(context)
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,this@LocationService,
                    Looper.getMainLooper())
            })
            // Define callback for failure in checking the device location settings.
            .addOnFailureListener(OnFailureListener { e ->
                // Processing when the device is a Huawei device and has HMS Core (APK) installed, but its settings do not meet the location requirements.
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae = e as ResolvableApiException
                        // Call startResolutionForResult to display a popup message requesting the user to enable relevant permissions.
                        //rae.startResolutionForResult(this, 0)
                    } catch (sie: IntentSender.SendIntentException) {
                        // TODO
                    }
                }
            })
    }

    fun stopRequests(){

    }


    override fun onLocationResult(location: LocationResult?) {
        super.onLocationResult(location)
        location?.let {
            val lastLocation=it.lastLocation
            Log.e("TAG","Location lat:${lastLocation.latitude}\t lon:${lastLocation.longitude}")
        }
    }
}