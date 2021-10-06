package com.dtse.demoandroid.senasoft

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import org.json.JSONException

class PurchaseActivity : AppCompatActivity() {

    companion object{
        const val DATA_SOURCE="SENASOFT_PREFERENCES"
        const val VIDAS="VIDAS"
        const val RESOLUTION_CODE=6666
        const val PURCHASE_CODE=7777
    }

    lateinit var comprar:Button
    lateinit var vidas:TextView
    val _totalVidas=MutableLiveData<Int>().apply { value=0 }
    lateinit var iapClient:IapClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        comprar=findViewById(R.id.comprar)
        vidas=findViewById(R.id.vidas)
        val preferences=getSharedPreferences(DATA_SOURCE,Context.MODE_PRIVATE)
        val totalVidas=preferences.getInt(VIDAS,5)
        _totalVidas.postValue(totalVidas)
        _totalVidas.observe(this){
            vidas.text="$it"
        }
        iapClient=Iap.getIapClient(this)

        initIap()
    }

    private fun initIap() {
        val task=iapClient.isEnvReady
        task.addOnSuccessListener{ getProducts() }

        task.addOnFailureListener{
            if(it is IapApiException){
                val status=it.status
                if (status.statusCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                    // HUAWEI ID is not signed in.
                    if (status.hasResolution()) {
                        try {
                            // 6666 is a constant defined by yourself.
                            // Open the sign-in screen returned.
                            status.startResolutionForResult(this@PurchaseActivity, RESOLUTION_CODE)
                        } catch (exp: IntentSender.SendIntentException) {
                            Log.e("IAP",exp.toString())
                        }
                    }
                } else if (status.statusCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                    // The current country/region does not support IAP.
                }
                else{
                    //otra cosa falló
                    Log.e("IAP",it.toString())
                }
            }
        }
    }

    private fun getProducts() {
        val productIdList: MutableList<String> = ArrayList()
        val productID="PackVida"
        productIdList.add(productID)
        val req = ProductInfoReq()
        req.priceType = 0
        req.productIds=productIdList
        iapClient.obtainProductInfo(req).addOnSuccessListener{
            //All OK
            val productList = it.productInfoList
            if(productList.isNotEmpty()){
                for (product in productList){
                    Log.e("IAP","${product.productName}\t${product.price}\t${product.productDesc}")
                    comprar.setOnClickListener { comparVidas(product) }
                }
            }
        }.addOnFailureListener{
            //Algo salió mal
        }

    }

    private fun comparVidas(product: ProductInfo) {
        val req = PurchaseIntentReq()
        req.productId = product.productId
        req.priceType = 0
        req.developerPayload="Necesito esta info de regreso"
        iapClient.createPurchaseIntent(req)
                .addOnSuccessListener{
                    val status = it.status
                    if (status.hasResolution()) {
                        try {
                            // 6666 is a constant defined by yourself.
                            // Open the checkout screen returned.
                            status.startResolutionForResult(this@PurchaseActivity, PURCHASE_CODE)
                        } catch (exp: IntentSender.SendIntentException) {
                            Log.e("TAG",exp.toString())
                        }
                    }
                }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RESOLUTION_CODE->{initIap()}
            PURCHASE_CODE ->{data?.let { processPurchase(it) }}
        }
    }

    private fun processPurchase(data: Intent) {
        val purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(data)
        when(purchaseResultInfo.returnCode){
            OrderStatusCode.ORDER_STATE_CANCEL->{/*El usuario canceló la compra*/}
            OrderStatusCode.ORDER_STATE_FAILED, OrderStatusCode.ORDER_PRODUCT_OWNED->{/*Alguna orden de compra no se ha confirmado*/}
            OrderStatusCode.ORDER_STATE_SUCCESS ->{// The payment is successful.
                val inAppPurchaseData = purchaseResultInfo.inAppPurchaseData
                val inAppPurchaseDataSignature = purchaseResultInfo.inAppDataSignature
                onPaymentSuccess(inAppPurchaseData,inAppPurchaseDataSignature)
            }
        }
    }

    private fun onPaymentSuccess(inAppPurchaseData: String, inAppPurchaseDataSignature: String) {
        val req = ConsumeOwnedPurchaseReq()
        var purchaseToken = ""
        try {
            // Obtain purchaseToken from InAppPurchaseData.
            val inAppPurchaseDataBean = InAppPurchaseData(inAppPurchaseData)
            purchaseToken = inAppPurchaseDataBean.purchaseToken
        } catch (e: JSONException) {
        }

        req.purchaseToken = purchaseToken

        var vidas=_totalVidas.value?:5
        vidas+=5//Sumamos las que acabamos de comprar

        _totalVidas.postValue(vidas)

        iapClient.consumeOwnedPurchase(req)
                .addOnSuccessListener{
                    //Salio bien
                }
                .addOnFailureListener{
                    //Algo salio mal
                }

    }


    override fun onDestroy() {
        val preferencesEditor=getSharedPreferences(DATA_SOURCE,Context.MODE_PRIVATE).edit()
        preferencesEditor.putInt(VIDAS,_totalVidas.value?:5)
        preferencesEditor.apply()
        super.onDestroy()
    }
}