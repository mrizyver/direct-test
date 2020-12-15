package com.sonerim.derect_wifi_test

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val intentFilter = IntentFilter()
    private  var channel: Channel? = null
    private var manager: WifiP2pManager? = null
    private var peers: Collection<WifiP2pDevice> = listOf()
    var isWifiP2pEnabled: Boolean = false
        set(value) {
            field = value
            Log.d(TAG, "isWifiP2pEnabled: $value")
        }

    companion object {
        const val SERVERPORT = 5000
        const val SERVER_IP = "192.168.4.1"
    }

    private val socket: AtomicReference<Socket?> = AtomicReference()
    val TAG = this::class.java.simpleName
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        peers = peerList.deviceList
        Log.d(TAG, "PeerListListener: ${peerList.deviceList.joinToString()}")
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val activity = this@MainActivity
            Log.d(TAG, "onReceive: ${intent?.action}")
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        throw RuntimeException()
                    }
                    manager?.requestPeers(channel?: return, peerListListener) ?: return
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        throw RuntimeException()
                    }
                    manager?.requestPeers(channel ?: return, peerListListener) ?: return
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val wifiP2pInfo = intent.getParcelableExtra<WifiP2pInfo>(EXTRA_WIFI_P2P_INFO)
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(EXTRA_NETWORK_INFO)
                    val wifiP2pGroup = intent.getParcelableExtra<WifiP2pGroup>(EXTRA_WIFI_P2P_GROUP)
                    if (networkInfo?.isConnected == true) {
                        manager?.requestConnectionInfo(channel, ConnectionInfoListener {
                            println()
                        })
                    }
                    println()
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val wifiP2pDevice =
                        intent.getParcelableExtra<WifiP2pDevice>(EXTRA_WIFI_P2P_DEVICE)
                    println()
                }
                else -> {
                    throw RuntimeException()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (false){
            thread {
                val serverSocket = ServerSocket(5000)
                val accept = serverSocket.accept()
                val inputStream = accept.getInputStream()
                val outputStream = accept.getOutputStream()
                while ( true ){
                    val read = inputStream.read()
                    outputStream.write(read)
                }
            }
        }else{
            Thread(ClientThread()).start()
            // Indicates a change in the Wi-Fi P2P status.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

            // Indicates a change in the list of available peers.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

            // Indicates the state of Wi-Fi P2P connectivity has changed.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

            // Indicates this device's details have changed.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            channel = manager?.initialize(this, mainLooper, null)
        }
    }

    public override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
        discoverService()
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun discoverService() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw java.lang.RuntimeException()
        }
        manager?.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure")
            }
        })
    }

    inner class ClientThread : Runnable {
        override fun run() {
            try {
                return
                val serverAddr = InetAddress.getByName(SERVER_IP)
                val socket1 = Socket(serverAddr, SERVERPORT)
                socket.set(socket1)
                val out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(
                            socket.get()?.getOutputStream()
                                ?: return
                        )
                    ), true
                )
                out.println("hello")
            } catch (e1: UnknownHostException) {
                e1.printStackTrace()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }
}