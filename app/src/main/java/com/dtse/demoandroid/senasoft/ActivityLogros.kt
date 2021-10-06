package com.dtse.demoandroid.senasoft

import android.content.Intent
import android.hardware.camera2.params.BlackLevelPattern
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.jos.JosApps
import com.huawei.hms.jos.games.AchievementsClient
import com.huawei.hms.jos.games.Games
import com.huawei.hms.jos.games.player.Player
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton


class ActivityLogros : AppCompatActivity() {
    companion object{
        const val TAG="Game Service"
        const val RED="RED"
        const val BLUE="BLUE"
        const val GREEN="GREEN"
        const val WHITE="WHITE"
    }

    private var achievementsClient:AchievementsClient?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logros)
        val appsClient = JosApps.getJosAppsClient(this)
        appsClient.init()
        Log.i(TAG, "init success")
        getRemoteParameters()
        getGamePlayer()
        enableGameButtons()

        //val hwid=findViewById<HuaweiIdAuthButton>(R.id.hwid)
        //hwid.setOnClickListener { loginHuawei() }
    }

    private fun getRemoteParameters() {
        val config = AGConnectConfig.getInstance()
        config.fetch(10).addOnSuccessListener{
            config.apply(it)
        }.addOnFailureListener{
            Log.e("TAG",it.toString())
        }.addOnCompleteListener{
            val color=when(config.getValueAsString("COLOR")){
                RED->resources.getColor(R.color.red,theme)
                BLUE->resources.getColor(R.color.blue,theme)
                GREEN->resources.getColor(R.color.green,theme)
                else ->{
                    resources.getColor(R.color.white,theme)
                 }
            }

            findViewById<ConstraintLayout>(R.id.layout).setBackgroundColor(color)
        }
    }


    fun getGamePlayer() {
        // Call the getPlayersClient method for initialization.
        val client = Games.getPlayersClient(this)
        // Obtain player information.
        val task: Task<Player> = client.gamePlayer
        task.addOnSuccessListener(OnSuccessListener<Player> { player ->
            val accessToken = player.accessToken
            val displayName = player.displayName
            val unionId = player.unionId
            val openId = player.openId
            // The player information is successfully obtained. Your game is started after accessToken is verified.
        }).addOnFailureListener(OnFailureListener { e ->
            if (e is ApiException) {
                val result = "rtnCode:" + e.statusCode
                // Failed to obtain player information. Rectify the fault based on the result code.
            }
        })
    }

    private fun enableGameButtons() {
        achievementsClient = Games.getAchievementsClient(this)
        findViewById<Button>(R.id.buttonView).setOnClickListener { viewAchievments() }
        findViewById<Button>(R.id.buttonSimple).setOnClickListener { unlockSimpleAchievment() }
        findViewById<Button>(R.id.buttonIncrementa).setOnClickListener { incrementSteps() }
        findViewById<Button>(R.id.buttonHidden).setOnClickListener { unlockHidden() }
    }

    private fun unlockHidden() {
        unlock("9A44C0A4E343DEAB71AE940189B4FD83B71AB102BACFC821F7390A071398133C")
    }

    private fun incrementSteps() {
        val id="A32815BB1FA79D5A189549492777CEC4399767A261423200E0CCC4B02C4C829E"
        achievementsClient?.let{client->
            val task: Task<Boolean> = client.growWithResult(id, 1)
            task.addOnSuccessListener { isSucess ->
                if (isSucess) {
                    Toast.makeText(this,"Se incrementó un paso",Toast.LENGTH_SHORT).show()
                } else {
                    Log.i("Achievement", "achievement can not grow")
                }
            }.addOnFailureListener { e ->
                if (e is ApiException) {
                    val result = ("rtnCode:"
                            + (e as ApiException).statusCode)
                    Log.e("Achievement", "result:$result")
                }
            }

        }
    }

    private fun unlockSimpleAchievment() {
        unlock("7611A4CFD75B618B756DB23633577D14EEC356AF35DD16019EF46FE892AA50B3")
    }

    private fun unlock(achievementId: String){
        achievementsClient?.let {
            val task: Task<Void> = it.reachWithResult(achievementId)
            task.addOnSuccessListener { Log.i("Achievement", "reach  success") }.addOnFailureListener { e ->
                if (e is ApiException) {
                    val result = ("rtnCode:"
                            + (e as ApiException).statusCode)
                    Log.e("Achievement", "reach result$result")
                }
            }
        }
    }

    private fun viewAchievments() {

        achievementsClient?.let { client->
            client.getAchievementList(true)
                    .addOnSuccessListener{
                        for(item in it){
                            Log.e(TAG, "${item.id} \t ${item.displayName}")
                        }
                    }

        }
        achievementsClient?.let{ client->
            client.showAchievementListIntent.addOnSuccessListener{
                try{
                    startActivityForResult(it, 1)
                }
                catch (e: Exception){
                    Log.e(TAG, "Activity is invalid")
                }
            }.addOnFailureListener{ e->
                Log.e(TAG, e.toString())
            }
        }
    }
}