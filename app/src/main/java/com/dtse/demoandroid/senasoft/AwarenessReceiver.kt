package com.dtse.demoandroid.senasoft

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huawei.hms.kit.awareness.barrier.BarrierStatus

class AwarenessReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "Receiver"
        val BARRIER_RECEIVER_ACTION =
            "com.dtse.demoandroid.senasoft.HEADSET_BARRIER_RECEIVER_ACTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val barrierStatus = BarrierStatus.extract(intent)
        val label = barrierStatus.barrierLabel

        when (label) {
            "headset connecting barrier" -> processHeadsetEvent(barrierStatus)
        }

    }

    private fun processHeadsetEvent(barrierStatus: BarrierStatus) {
        when (barrierStatus.presentStatus) {
            BarrierStatus.TRUE -> Log.i(TAG, "Headset status:connected")
            BarrierStatus.FALSE -> Log.i(TAG, "Headset status:disconnected")
            BarrierStatus.UNKNOWN -> Log.i(TAG, "Headset status:unknown")
        }
    }

}