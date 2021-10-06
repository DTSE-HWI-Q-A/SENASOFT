package com.dtse.demoandroid.senasoft

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier
import com.huawei.hms.panorama.Panorama
import com.huawei.hms.panorama.PanoramaInterface
import com.huawei.hms.panorama.PanoramaInterface.ImageInfoResult
import com.huawei.hms.support.api.client.ResultCallback


class InmersiveActivity : AppCompatActivity() {
    private var panoramaInterface: PanoramaInterface.PanoramaLocalInterface? = null
    private lateinit var container: FrameLayout
    private val barrierReceiver = AwarenessReceiver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inmersive)
        container = findViewById(R.id.container)
        findViewById<Button>(R.id.fullscrbtn).setOnClickListener { fullScreenView() }
        val uri = getUriFromResource(R.raw.panorama)
        initPanorama(uri)
        initAwareness()
    }

    private fun initAwareness() {
        var headsetBarrier : AwarenessBarrier = HeadsetBarrier.connecting()
        val intent = Intent(AwarenessReceiver.BARRIER_RECEIVER_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        registerReceiver(barrierReceiver, IntentFilter(AwarenessReceiver.BARRIER_RECEIVER_ACTION))

        val headsetBarrierLabel = "headset connecting barrier"
        val builder = BarrierUpdateRequest.Builder()
// Define a request for updating a barrier.
        val request = builder.addBarrier(headsetBarrierLabel, headsetBarrier, pendingIntent).build()
        Awareness.getBarrierClient(this).updateBarriers(request)
            // Callback listener for execution success.
            .addOnSuccessListener { Toast.makeText(applicationContext, "add barrier success", Toast.LENGTH_SHORT).show()
            }
            // Callback listener for execution failure.
            .addOnFailureListener { e ->
                unregisterReceiver(barrierReceiver)
                Toast.makeText(applicationContext, "add barrier failed", Toast.LENGTH_SHORT).show()
                Log.e("Awareness", "add barrier failed", e)
            }
    }

    private fun initPanorama(uri: Uri) {
        panoramaInterface = Panorama.getInstance().getLocalInstance(this)
        panoramaInterface?.let { panorama ->
            if (panorama.init() == 0) {
                if (panorama.setImage(uri, PanoramaInterface.IMAGE_TYPE_RING) == 0) {
                    panorama.setControlMode(PanoramaInterface.CONTROL_TYPE_POSE)
                    container.addView(panorama.view)
                    panorama.view.setOnTouchListener { v, event ->
                        panorama.view.performClick()
                        panorama.updateTouchEvent(event)
                        true
                    }
                } else Log.e("Panorama", "Failed to load panorama")

            } else Log.e("Panorama", "fail to init panorama")
        }

    }

    private fun getUriFromResource(resourceId: Int): Uri {
        return Uri.parse("android.resource://$packageName/$resourceId")
    }

    private fun fullScreenView() {
        Panorama.getInstance().loadImageInfo(this, getUriFromResource(R.raw.panorama),PanoramaInterface.IMAGE_TYPE_RING)
            .setResultCallback { panoramaResult ->
                // The panorama is successfully loaded.
                if (panoramaResult.status.isSuccess) {
                    val intent = panoramaResult.imageDisplayIntent
                    intent?.let { startActivity(it) }
                } else {
                    Log.e("Panorama", "error: $panoramaResult")
                }
            }
    }

    override fun onDestroy() {
        unregisterReceiver(barrierReceiver)
        val headsetBarrierLabel = "headset connecting barrier"
        val builder = BarrierUpdateRequest.Builder()
// Define a request for updating a barrier.
        val request = builder.deleteBarrier(headsetBarrierLabel).build()
        Awareness.getBarrierClient(this).updateBarriers(request)
            // Callback listener for execution success.
            .addOnSuccessListener { Toast.makeText(applicationContext, "delete barrier success", Toast.LENGTH_SHORT).show()
            }
            // Callback listener for execution failure.
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "delete barrier failed", Toast.LENGTH_SHORT).show()
                Log.e("Awareness", "delete barrier failed", e)
            }
        super.onDestroy()
    }
}
