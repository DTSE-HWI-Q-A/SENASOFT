package com.dtse.demoandroid.senasoft

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class MyHmsMessageService:HmsMessageService() {
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        token?.let {
            Log.e("TOKEN",it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let{
            val map=it.dataOfMap
            Log.e("Mensaje",map["message"]?:"Mensaje nulo")
        }
    }
}