package com.dtse.demoandroid.senasoft

import android.app.Application
import android.os.Bundle
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import com.huawei.hms.ads.HwAds
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.hms.analytics.type.ReportPolicy
import com.huawei.hms.api.HuaweiMobileServicesUtil
import com.huawei.hms.jos.JosApps


class MyApplication:Application() {
    override fun onCreate() {
        HuaweiMobileServicesUtil.setApplication(this)
        val instance = HiAnalytics.getInstance(this)
        HiAnalyticsTools.enableLog()
        // Create a policy that is used to report an event upon app switching to the background.
        val moveBackgroundPolicy = ReportPolicy.ON_MOVE_BACKGROUND_POLICY
// Create a policy that is used to report an event at the specified interval.
        val scheduledTimePolicy = ReportPolicy.ON_SCHEDULED_TIME_POLICY
// Set the event reporting interval to 600 seconds.
        scheduledTimePolicy.threshold = 600
        val reportPolicies = HashSet<ReportPolicy>()
// Add the ON_SCHEDULED_TIME_POLICY and ON_MOVE_BACKGROUND_POLICY policies.
        reportPolicies.add(scheduledTimePolicy)
        reportPolicies.add(moveBackgroundPolicy)
// Set the ON_MOVE_BACKGROUND_POLICY and ON_SCHEDULED_TIME_POLICY policies.
        instance.setReportPolicies(reportPolicies)


        val data=Bundle()
        data.putString("hp", "100")
        instance.onEvent("Nivel50", data)
        HwAds.init(this)

        //Remote Config
        val config = AGConnectConfig.getInstance()
        val map = mutableMapOf<String,Any>()
        map["COLOR"] = "WHITE"
        map["NUMBER"]=1
        map["BOOLEAN"]=true
        config.applyDefault(map)

        super.onCreate()
    }
}