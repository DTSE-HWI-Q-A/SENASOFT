package com.dtse.demoandroid.senasoft

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        findViewById<HuaweiIdAuthButton>(R.id.hwid).setOnClickListener {
            loginHwid()
        }
    }

    private fun loginHwid() {
        val authParams= AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken()
            .createParams()

        val mAuthService= AccountAuthManager.getService(this, authParams)
        val signInIntent=mAuthService.signInIntent
        startActivityForResult(signInIntent, 200)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==200){
            val task=AccountAuthManager.parseAuthResultFromIntent(data)
            if(task.isSuccessful){
                val intent=Intent(this,ActivityLogros::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}