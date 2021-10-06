package com.dtse.demoandroid.senasoft

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.api.HuaweiMobileServicesUtil
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.api.CommonStatusCodes
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityRequest
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckThreat
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class SecurityActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Security"
        const val RS256 = "RS256"
        const val PS256 = "PS256"
        const val MALWARE = 1
        const val PHISHING = 3
        const val APP_ID="104767731"
    }

    lateinit var client:SafetyDetectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)
        client=SafetyDetect.getClient(this)
        client.initUrlCheck()
        initUserDetect()
        invokeSysIntegrity()
        val urlBox=findViewById<EditText>(R.id.urlBox)
        findViewById<Button>(R.id.urlCheck).setOnClickListener {
            val link=urlBox.text
            if(!TextUtils.isEmpty(link)){
                invokeUrlCheck(link.toString())
            }
        }
        getMaliciousApps()
    }

    private fun initUserDetect() {
        // Replace with your activity or context as a parameter.
        client.initUserDetect().addOnSuccessListener {
            findViewById<Button>(R.id.humanCheck).setOnClickListener { userDetect() }
        }.addOnFailureListener {
            // An error occurred during communication with the service.
        }
    }

    fun userDetect() {
        //val client = SafetyDetect.getClient(activity)
        val appId = APP_ID
        client.userDetection(appId)
            .addOnSuccessListener { userDetectResponse ->
                // Indicates that communication with the service was successful.
                val responseToken = userDetectResponse.responseToken
                if (responseToken.isNotEmpty()) {
                    // Send the response token to your app server, and call the cloud API of HMS Core on your server to obtain the fake user detection result.
                    showDialog("User detect","No eres un robot")
                }else{
                    showDialog("UserDetect","Eres un robot")
                }
            }
            .addOnFailureListener {  // There was an error communicating with the service.
                val errorMsg: String? = if (it is ApiException) {
                    // An error with the HMS API contains some additional details.
                    // You can use the apiException.getStatusCode() method to get the status code.
                    (SafetyDetectStatusCodes.getStatusCodeString(it.statusCode) + ": "
                            + it.message)
                } else {
                    // Unknown type of error has occurred.
                    it.message
                }
                Log.i(TAG, "User detection fail. Error info: $errorMsg")
            }
    }

    private fun shutdownUserDetect() {
        // Replace with your activity or context as a parameter.
        val client = SafetyDetect.getClient(this)
        client.shutdownUserDetect()
            .addOnSuccessListener {
                // Indicates that communication with the service was successful.
            }.addOnFailureListener {
                // An error occurred during communication with the service.
            }
    }

    override fun onDestroy() {
        shutdownUserDetect()
        super.onDestroy()
        Build.VERSION_CODES.Q
    }

    private fun invokeUrlCheck(link:String) {
        client.urlCheck(
            link,
            APP_ID,  // Specify url threat type
            UrlCheckThreat.MALWARE,
            UrlCheckThreat.PHISHING
        ).addOnSuccessListener {
            val list = it.urlCheckResponse
            if (list.isEmpty()) {
                // No threat exists.
                showDialog("URL Ckeck","Este link es seguro")
            } else {
                // Threats exist.
                showDialog("URL Ckeck","Este link no es seguro")
            }
        }.addOnFailureListener {
            // An error occurred during communication with the service.
            if (it is ApiException) {
                // HMS Core (APK) error code and corresponding error description.
                val apiException = it
                Log.d(
                    TAG,
                    "Error: " + CommonStatusCodes.getStatusCodeString(apiException.statusCode)
                )
                // Note: If the status code is SafetyDetectStatusCode.CHECK_WITHOUT_INIT,
                // you did not call the initUrlCheck() method or you have initiated a URL check request before the call is completed.
                // If an internal error occurs during the initialization, you need to call the initUrlCheck() method again to initialize the API.
            } else {
                // An unknown exception occurs.
                Log.d(TAG, "Error: " + it.message)
            }
        }
    }

    private fun getMaliciousApps() {
        client
            .maliciousAppsList
            .addOnSuccessListener { maliciousAppsListResp ->
                val appsDataList: List<MaliciousAppsData> = maliciousAppsListResp.maliciousAppsList
                if (maliciousAppsListResp.rtnCode == CommonCode.OK) {
                    if (appsDataList.isEmpty()) {
                        val text = "No known potentially malicious apps are installed."
                        showDialog("maliciousApps",text)
                        //Toast.makeText(this!!.applicationContext, text, Toast.LENGTH_SHORT).show()
                    } else {
                        val appCheck=findViewById<TextView>(R.id.appCheck)
                        val sb=StringBuilder()
                        for (maliciousApp in appsDataList) {
                            sb.append("Information about a malicious app:")
                            sb.append( "  APK: ${maliciousApp.apkPackageName}")
                            sb.append("  SHA-256: ${maliciousApp.apkSha256}")
                            sb.append( "  Category: ${maliciousApp.apkCategory}\n")
                        }
                        appCheck.text=sb.toString()
                    }
                } else {
                    val msg = ("Get malicious apps list failed! Message: "
                            + maliciousAppsListResp.errorReason)
                    //Log.e(com.huawei.hms.safetydetect.sample.SafetyDetectAppsCheckAPIFragment.TAG, msg)
                    //Toast.makeText(this!!.applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e -> // There was an error communicating with the service.
                val errorMsg: String? = if (e is ApiException) {
                    // An error with the HMS API contains some additional details.
                    val apiException = e as ApiException
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
                    // You can use the apiException.getStatusCode() method to get the status code.
                } else {
                    // Unknown type of error has occurred.
                    e.message
                }
                val msg = "Get malicious apps list failed! Message: $errorMsg"
                //Log.e(com.huawei.hms.safetydetect.sample.SafetyDetectAppsCheckAPIFragment.TAG, msg)
                //Toast.makeText(this!!.applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun invokeSysIntegrity() {
        val nonce = ByteArray(24)
        try {
            val random: SecureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong()
            } else {
                SecureRandom.getInstance("SHA1PRNG")
            }
            random.nextBytes(nonce)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, e.toString())
        }
// TODO (developer): Change your app ID. You can obtain your app ID in AppGallery Connect.
        val sysIntegrityRequest = SysIntegrityRequest()
        sysIntegrityRequest.appId = "104767731"
        sysIntegrityRequest.nonce = nonce
        sysIntegrityRequest.alg = PS256
        client.sysIntegrity(sysIntegrityRequest)
            .addOnSuccessListener { response -> // Indicates communication with the service was successful.
                // Use response.getResult() to obtain the result data.
                val jwsStr = response.result
                Log.e(TAG, jwsStr)
// Process the result data here.
                val jwsSplit = jwsStr.split(".").toTypedArray()
                val jwsPayloadStr = jwsSplit[1]
                val payloadDetail = String(
                    Base64.decode(
                        jwsPayloadStr.toByteArray(StandardCharsets.UTF_8),
                        Base64.URL_SAFE
                    ), StandardCharsets.UTF_8
                )
                try {
                    val jsonObject = JSONObject(payloadDetail)
                    val basicIntegrity = jsonObject.getBoolean("basicIntegrity")
                    val isBasicIntegrity = basicIntegrity.toString()
                    val basicIntegrityResult = "Basic Integrity: $isBasicIntegrity"
                    findViewById<TextView>(R.id.sysCheck).text=basicIntegrityResult
                    if (!basicIntegrity) {
                        val advise=jsonObject.getString("advice")
                        showDialog("Verification Failed", advise)
                    }
                } catch (e: JSONException) {
                    val errorMsg = e.message
                    Log.e(TAG, errorMsg ?: "unknown error")
                }


            }
            .addOnFailureListener { e -> // There was an error communicating with the service.
                val errorMsg: String? = if (e is ApiException) {
// An error with the HMS API contains some additional details.
                    val apiException = e as ApiException
                    SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) +
                            ": " + apiException.message
// You can use the apiException.getStatusCode() method to obtain the status code.
                } else {
// An unknown type of error has occurred.
                    e.message
                }
                errorMsg?.let {
                    Log.e(TAG, it)
                    showDialog("Verification Failed", it)
                }

            }
    }

    private fun showDialog(title: String, message: String){
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK"){ dialogInterface, _->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }
}