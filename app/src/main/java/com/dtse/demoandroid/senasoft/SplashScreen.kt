package com.dtse.demoandroid.senasoft

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        silentSignInHw()
    }

    private fun silentSignInHw() {
        val authParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .createParams()

        val mAuthService = AccountAuthManager.getService(this, authParams)
        mAuthService.silentSignIn()
                .addOnSuccessListener {
                    //jump("pass")
                    jumpClass(InmersiveActivity::class.java)
                }
                .addOnFailureListener{
                    jump("login")
                }
    }

    private fun jumpClass(target: Class<*>){
        val handler=Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent=Intent(this,target)
            startActivity(intent)
            finish()
        },1000)
    }

    private fun jump(target:String) {
        val handler=Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent=
                when(target){
                    "login"->Intent(this,LoginActivity::class.java)
                    "pass"-> Intent(this,ActivityLogros::class.java)
                    else->Intent(this,LoginActivity::class.java)
                }
            startActivity(intent)
            finish()
        },5000)
    }


}