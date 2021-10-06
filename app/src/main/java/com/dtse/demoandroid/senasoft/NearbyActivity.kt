package com.dtse.demoandroid.senasoft

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.huawei.hms.nearby.discovery.ScanEndpointInfo
import com.huawei.hms.nearby.transfer.Data
import java.nio.charset.StandardCharsets

class NearbyActivity : AppCompatActivity(), NearbyService.NearbyServiceListener {
    private var service:NearbyService?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)
        if (checkLocationPermissions())setupNearby()
        else requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),100)

    }

    private fun setupNearby() {
        service= NearbyService("${Build.MANUFACTURER} ${Build.MODEL}",applicationContext)
        service?.nearbyServiceListener=this
        findViewById<Button>(R.id.clientBtn).setOnClickListener { configClient() }
        findViewById<Button>(R.id.ServerBtn).setOnClickListener { configServer() }
    }

    private fun checkLocationPermissions():Boolean {
        val acl=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val afl=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return acl==PackageManager.PERMISSION_GRANTED&&afl==PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkLocationPermissions()) setupNearby()
    }

    private fun configServer() {
        service?.setupServer()
    }

    private fun configClient() {
        service?.setupClient()
    }

    override fun onConnected(endpointId: String,endpointName:String) {
        showToast("Se estableció la conexión con $endpointName")
        findViewById<Button>(R.id.helloBtn).setOnClickListener {
            val message="Hola"
            service?.sendData(message.toByteArray(StandardCharsets.UTF_8))
        }
    }

    override fun onDisconnected(endpointId: String) {
        if(endpointId=="not found"){
            showToast("No se encontraron dispositivos")
        }else showToast("desconectado de $endpointId")
        findViewById<Button>(R.id.helloBtn).setOnClickListener(null)
    }

    override fun onLost(endpointId: String) {
        showToast("$endpointId dejó de estar disponible")
    }

    override fun onFound(endpointId: String, discoveryEndpointInfo: ScanEndpointInfo) {
        AlertDialog.Builder(this)
            .setTitle("Nearby Service")
            .setCancelable(false)
            .setMessage("Conectar con ${discoveryEndpointInfo.name}?")
            .setPositiveButton("Sí"){dialogInterface,_->
                service?.connectToServer("${Build.MANUFACTURER} ${Build.MODEL}",endpointId)
                dialogInterface.dismiss()
            }
            .setNegativeButton("No"){dialogInterface,_->
                dialogInterface.dismiss()
            }
            .create().show()
    }

    override fun onData(data: Data) {
        // Recibir datos
        val message= String(data.asBytes(),StandardCharsets.UTF_8)
        showToast("Mensaje recibido: $message")
    }

    fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }


}