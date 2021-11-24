package com.example.nearbyclient

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nearbyclient.databinding.ActivityMainBinding
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val DISCOVERYMODE = 1
    private val ADVERTOSINGMODE = 2

    private var startMode = 0

    private lateinit var binding: ActivityMainBinding

    private val REQUEST_PERMISSIONS = arrayOf(
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.CHANGE_WIFI_STATE",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    private val REQUEST_CODE_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(allPermissionGranted()){
            Log.v("TONIWESTERLUND", "PERMISSION OK")
        }else{
            Log.v("TONIWESTERLUND", "PERMISSION NOT OK")
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS,REQUEST_CODE_PERMISSIONS)
        }

        binding.buttonOne.setOnClickListener{
            Log.v("TONIWESTERLUND", "startAdvertising")
            startMode = ADVERTOSINGMODE
            startAdvertising()
        }
        binding.buttonTwo.setOnClickListener{
            Log.v("TONIWESTERLUND", "startDiscovery")
            startMode = DISCOVERYMODE
            startDiscovery()
        }
    }

    private fun allPermissionGranted() : Boolean{
        for (permission in REQUEST_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionGranted()){
                Log.v("TONIWESTERLUND", "onRequestPermissionsResult - PERMISSION OK")
            }else{
                Log.v("TONIWESTERLUND", "onRequestPermissionsResult - PERMISSION OK")
            }
        }



    }

    private fun startDiscovery(){
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()

        Nearby.getConnectionsClient(this).startDiscovery(
            "WESTERLUND",endpointDiscoveryCallback,discoveryOptions
        ).addOnSuccessListener {
                a : Void? -> Log.v("TONIWESTERLUND", "startDiscovery addOnSuccessListener")
        }.addOnFailureListener {
                a : Exception? -> Log.v("TONIWESTERLUND", "startDiscovery addOnFailureListener")
        }

    }

    private fun startAdvertising(){

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(
            Strategy.P2P_POINT_TO_POINT).build()

        Nearby.getConnectionsClient(this).startAdvertising("TONI","WESTERLUND",
            connectionLifecycleCallback,advertisingOptions).addOnSuccessListener {
                a : Void? -> Log.v("TONIWESTERLUND", "startAdvertising addOnSuccessListener")
            }.addOnFailureListener {
                a : Exception? -> Log.v("TONIWESTERLUND", "startAdvertising addOnFailureListener")
            }

    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            Log.v("TONIWESTERLUND", "onConnectionInitiated")
            //if(ADVERTOSINGMODE == startMode){
                Log.v("TONIWESTERLUND", "Accept Connection")
                Nearby.getConnectionsClient(this@MainActivity).acceptConnection(p0,payloadCallback)
            //}
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            Log.v("TONIWESTERLUND", "oonConnectionResult")


            if(ADVERTOSINGMODE == startMode){

                when(p1.status.statusCode){
                    ConnectionsStatusCodes.STATUS_OK -> {
                        Log.v("TONIWESTERLUND", "STATUS_OK")
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        Log.v("TONIWESTERLUND", "STATUS_CONNECTION_REJECTED")
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        Log.v("TONIWESTERLUND", "STATUS_ERROR")
                    }
                }
            }
            if(DISCOVERYMODE == startMode){

                when(p1.status.statusCode){
                    ConnectionsStatusCodes.STATUS_OK -> {
                        Log.v("TONIWESTERLUND", "STATUS_OK")
                        sendPayLoad(p0)

                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        Log.v("TONIWESTERLUND", "STATUS_CONNECTION_REJECTED")
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        Log.v("TONIWESTERLUND", "STATUS_ERROR")
                    }
                }
            }
        }

        override fun onDisconnected(p0: String) {
            Log.v("TONIWESTERLUND", "oonDisconnected")
        }

    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback(){
        override fun onEndpointFound(p0: String, p1: DiscoveredEndpointInfo) {
            Log.v("TONIWESTERLUND", "onEndpointFound")

            Nearby.getConnectionsClient(this@MainActivity).requestConnection("TONI",p0,connectionLifecycleCallback)
        }

        override fun onEndpointLost(p0: String) {
            Log.v("TONIWESTERLUND", "onEndpointLost")
        }

    }

    private val payloadCallback = object : PayloadCallback(){
        override fun onPayloadReceived(p0: String, p1: Payload) {
            Log.v("TONIWESTERLUND", "onPayloadReceived")

            val receivedBytes : ByteArray? = p1.asBytes()
            var message = receivedBytes?.let { String(it) }

            if(message != null){
                Log.v("TONIWESTERLUND", "onPayloadReceived  $message")
            }

        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            Log.v("TONIWESTERLUND", "onPayloadTransferUpdate")
        }

    }

    private fun sendPayLoad(endId : String){
        var message : String = "HELLOWORLD"
        val bytesPayLoad = Payload.fromBytes(message.toByteArray())
        Nearby.getConnectionsClient(this).sendPayload(endId,bytesPayLoad).addOnSuccessListener {
            Log.v("TONIWESTERLUND", "sendPayLoad addOnSuccessListener")
        }
            .addOnFailureListener {
                Log.v("TONIWESTERLUND", "sendPayLoad addOnSuccessListener")
            }
    }



}