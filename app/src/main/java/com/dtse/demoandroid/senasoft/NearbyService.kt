package com.dtse.demoandroid.senasoft

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.huawei.hms.nearby.Nearby
import com.huawei.hms.nearby.StatusCode
import com.huawei.hms.nearby.discovery.*
import com.huawei.hms.nearby.transfer.Data
import com.huawei.hms.nearby.transfer.DataCallback
import com.huawei.hms.nearby.transfer.TransferEngine
import com.huawei.hms.nearby.transfer.TransferStateUpdate

class NearbyService(val id: String, val context: Context) : ConnectCallback() {

    companion object {
        const val SERVICE_ID = "SENASOFT"
        private const val TAG = "NearbyService"
        private const val TIMEOUT_MILLISECONDS = 10000
    }

    private var mDiscoveryEngine: DiscoveryEngine? = null
    private var mTransferEngine: TransferEngine? = null
    private var endpointId: String? = null
    private var connectTaskResult = 0
    private var isServer:Boolean=false
    var nearbyServiceListener:NearbyServiceListener?=null

    init {
        mDiscoveryEngine = Nearby.getDiscoveryEngine(context)
        connectTaskResult = StatusCode.STATUS_ENDPOINT_UNKNOWN
    }


    private val scanEndpointCallback:ScanEndpointCallback=object: ScanEndpointCallback() {
        override fun onFound(endpointId: String?, discoveryEndpointInfo: ScanEndpointInfo?) {
            if(endpointId!=null&&discoveryEndpointInfo!=null){
                nearbyServiceListener?.onFound(endpointId,discoveryEndpointInfo)
            }
        }

        override fun onLost(endpointId: String?) {
            endpointId?.let {
                nearbyServiceListener?.onLost(it)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            this.removeMessages(0)
            if (connectTaskResult != StatusCode.STATUS_SUCCESS) {
                onDisconnected("not found")

                if (isServer) {
                    mDiscoveryEngine?.stopBroadcasting()
                } else {
                    mDiscoveryEngine?.stopScan()
                }
            }
        }
    }

    private val dataCallback: DataCallback=object : DataCallback() {
        override fun onReceived(endpointId: String?, data: Data?) {
            data?.let {
                nearbyServiceListener?.onData(it)
            }
        }

        override fun onTransferUpdate(endpointId: String?, status: TransferStateUpdate?) {

        }

    }

    fun setupServer(){
        isServer=true
        val advBuilder = BroadcastOption.Builder()
        advBuilder.setPolicy(Policy.POLICY_P2P)
        mDiscoveryEngine?.startBroadcasting(id, SERVICE_ID, this, advBuilder.build())
        handler.sendEmptyMessageDelayed(0, TIMEOUT_MILLISECONDS.toLong())
    }

    fun setupClient(){
        isServer=false
        Log.e(TAG, "startScanning()")
        val discBuilder = ScanOption.Builder()
        discBuilder.setPolicy(Policy.POLICY_P2P)
        mDiscoveryEngine?.startScan(SERVICE_ID, scanEndpointCallback, discBuilder.build())
        handler.sendEmptyMessageDelayed(0, TIMEOUT_MILLISECONDS.toLong())
    }

    fun connectToServer(myName: String, endpointId: String){
        this.endpointId = endpointId
        mDiscoveryEngine?.requestConnect(myName, endpointId, this)

    }

    override fun onEstablish(endpointId: String?, connectInfo: ConnectInfo?) {
        //Confirmar la conexión
        this.endpointId=endpointId
        mDiscoveryEngine?.acceptConnect(endpointId, dataCallback)
        mTransferEngine=Nearby.getTransferEngine(context)
        connectTaskResult = StatusCode.STATUS_SUCCESS
        //if (listener != null) listener.onConnection()
        if(isServer) mDiscoveryEngine?.stopBroadcasting()
        else mDiscoveryEngine?.stopScan()


        if(endpointId!=null&&connectInfo!=null){
            nearbyServiceListener?.onConnected(endpointId,connectInfo.endpointName)
        }
    }

    override fun onResult(endpointId: String?, connectResult: ConnectResult?) {

    }

    override fun onDisconnected(endpointId: String?) {
        endpointId?.let {
            nearbyServiceListener?.onDisconnected(it)
        }
    }

    fun sendData(data:ByteArray){
        val message=Data.fromBytes(data)
        mTransferEngine?.sendData(endpointId,message)
    }


    interface NearbyServiceListener{
        fun onConnected(endpointId: String, endpointName:String)
        fun onDisconnected(endpointId: String)
        fun onLost(endpointId: String)
        fun onFound(endpointId: String,discoveryEndpointInfo: ScanEndpointInfo)
        fun onData(data: Data)
    }
}