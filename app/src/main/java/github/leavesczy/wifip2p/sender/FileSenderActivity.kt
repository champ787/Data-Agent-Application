package github.leavesczy.wifip2p.sender

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import github.leavesczy.wifip2p.BaseActivity
import github.leavesczy.wifip2p.DeviceAdapter
import github.leavesczy.wifip2p.DirectActionListener
import github.leavesczy.wifip2p.DirectBroadcastReceiver
import github.leavesczy.wifip2p.OnItemClickListener
import github.leavesczy.wifip2p.R
import github.leavesczy.wifip2p.algorithm
import github.leavesczy.wifip2p.models.ViewState
import github.leavesczy.wifip2p.utils.WifiP2pUtils
import kotlinx.coroutines.launch


@SuppressLint("NotifyDataSetChanged")
class FileSenderActivity : BaseActivity() {
    private val handler = Handler()
    private val interval = 5000 // 5 second

    private val tvDeviceState by lazy {
        findViewById<TextView>(R.id.tvDeviceState)
    }

    private val tvConnectionStatus by lazy {
        findViewById<TextView>(R.id.tvConnectionStatus)
    }

    private val btnDisconnect by lazy {
        findViewById<Button>(R.id.btnDisconnect)
    }

    private val btnChooseFile by lazy {
        findViewById<Button>(R.id.btnChooseFile)
    }

    private val rvDeviceList by lazy {
        findViewById<RecyclerView>(R.id.rvDeviceList)
    }

    private val tvLog by lazy {
        findViewById<TextView>(R.id.tvLog)
    }

    private val btnDirectDiscover by lazy {
        findViewById<Button>(R.id.btnDirectDiscover)
    }
  /*added*/

    private val btnCalculateprogress by lazy {
        findViewById<Button>(R.id.btnCalculate)
    }

    private val fileSenderViewModel by viewModels<FileSenderViewModel>()

    private val getContentLaunch = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            val ipAddress = wifiP2pInfo?.groupOwnerAddress?.hostAddress
            log("getContentLaunch $imageUri $ipAddress")
            if (!ipAddress.isNullOrBlank()) {
//                fileSenderViewModel.send(ipAddress = ipAddress, fileUri = imageUri)
                startRepeatingCall(ipAddress = ipAddress, fileUri = imageUri)

            }
        }
    }

    private val wifiP2pDeviceList = mutableListOf<WifiP2pDevice>()

    private val deviceAdapter = DeviceAdapter(wifiP2pDeviceList)

    private var broadcastReceiver: BroadcastReceiver? = null

    private lateinit var wifiP2pManager: WifiP2pManager

    private lateinit var wifiP2pChannel: WifiP2pManager.Channel

    private var wifiP2pInfo: WifiP2pInfo? = null

    private var wifiP2pEnabled = false

    private val directActionListener = object : DirectActionListener {

        override fun wifiP2pEnabled(enabled: Boolean) {
            wifiP2pEnabled = enabled
        }

        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            dismissLoadingDialog()
            wifiP2pDeviceList.clear()
            deviceAdapter.notifyDataSetChanged()
            btnDisconnect.isEnabled = true
            btnChooseFile.isEnabled = true
            log("onConnectionInfoAvailable")
            log("onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed)
            log("onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner)
            log("onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.hostAddress)
            val stringBuilder = StringBuilder()
            stringBuilder.append("\n")
            stringBuilder.append("Whether the group owner：")
            stringBuilder.append(if (wifiP2pInfo.isGroupOwner) "is the group owner" else "Non-group owner")
            stringBuilder.append("\n")
            stringBuilder.append("Group owner IP address：")
            stringBuilder.append(wifiP2pInfo.groupOwnerAddress.hostAddress)
            tvConnectionStatus.text = stringBuilder
            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                this@FileSenderActivity.wifiP2pInfo = wifiP2pInfo
            }
        }

        override fun onDisconnection() {
            log("onDisconnection")
            btnDisconnect.isEnabled = false
            btnChooseFile.isEnabled = false
            wifiP2pDeviceList.clear()
            deviceAdapter.notifyDataSetChanged()
            tvConnectionStatus.text = null
            wifiP2pInfo = null
            showToast("in disconnected state")
        }

        override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice) {
            log("onSelfDeviceAvailable")
            log("DeviceName: " + wifiP2pDevice.deviceName)
            log("DeviceAddress: " + wifiP2pDevice.deviceAddress)
            log("Status: " + wifiP2pDevice.status)
            val log = "deviceName：" + wifiP2pDevice.deviceName + "\n" +
                    "deviceAddress：" + wifiP2pDevice.deviceAddress + "\n" +
                    "deviceStatus：" + WifiP2pUtils.getDeviceStatus(wifiP2pDevice.status)
            tvDeviceState.text = log
        }

        override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
            log("onPeersAvailable :" + wifiP2pDeviceList.size)
            this@FileSenderActivity.wifiP2pDeviceList.clear()
            this@FileSenderActivity.wifiP2pDeviceList.addAll(wifiP2pDeviceList)
            deviceAdapter.notifyDataSetChanged()
            dismissLoadingDialog()
        }

        override fun onChannelDisconnected() {
            log("onChannelDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender)

        /* new changes added*/
      btnCalculateprogress.setOnClickListener {
          val intent=Intent(this, algorithm::class.java)
          startActivity(intent)
      }

        /* new changes end*/

        initView()
        initDevice()
        initEvent()
    }

    @SuppressLint("MissingPermission")
    private fun initView() {
        supportActionBar?.title = "file sender"
        btnDisconnect.setOnClickListener {
            disconnect()
        }
        btnChooseFile.setOnClickListener {
          // getContentLaunch.launch("image/*")
            fileSenderViewModel.filesent=false
            getContentLaunch.launch("*/*")
        }
        btnDirectDiscover.setOnClickListener {
            if (!wifiP2pEnabled) {
                showToast("need to open first Wifi")
                return@setOnClickListener
            }
            showLoadingDialog(message = "Searching for nearby devices")
            wifiP2pDeviceList.clear()
            deviceAdapter.notifyDataSetChanged()
            wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    showToast("discoverPeers Success")
                    dismissLoadingDialog()
                }

                override fun onFailure(reasonCode: Int) {
                    showToast("discoverPeers Failure：$reasonCode")
                    dismissLoadingDialog()
                }
            })
        }
        deviceAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val wifiP2pDevice = wifiP2pDeviceList.getOrNull(position)
                if (wifiP2pDevice != null) {
                    connect(wifiP2pDevice = wifiP2pDevice)
                }
            }
        }
        rvDeviceList.adapter = deviceAdapter
        rvDeviceList.layoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
    }

    private fun initDevice() {
        val mWifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as? WifiP2pManager
        if (mWifiP2pManager == null) {
            finish()
            return
        }
        wifiP2pManager = mWifiP2pManager
        wifiP2pChannel = mWifiP2pManager.initialize(this, mainLooper, directActionListener)
        broadcastReceiver =
            DirectBroadcastReceiver(mWifiP2pManager, wifiP2pChannel, directActionListener)
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter())
    }

    private fun initEvent() {
        lifecycleScope.launch {
            fileSenderViewModel.viewState.collect {
                when (it) {
                    ViewState.Idle -> {
                        clearLog()
                        dismissLoadingDialog()
                    }

                    ViewState.Connecting -> {
                        showLoadingDialog(message = "")
                    }

                    is ViewState.Receiving -> {
                        showLoadingDialog(message = "")
                    }

                    is ViewState.Success -> {
                        dismissLoadingDialog()
                    }

                    is ViewState.Failed -> {
                        dismissLoadingDialog()
                    }
                }
            }
        }
        lifecycleScope.launch {
            fileSenderViewModel.log.collect {
                log(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(wifiP2pDevice: WifiP2pDevice) {
        val wifiP2pConfig = WifiP2pConfig()
        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress
        wifiP2pConfig.wps.setup = WpsInfo.PBC
        showLoadingDialog(message = "connecting，deviceName: " + wifiP2pDevice.deviceName)
        showToast("connecting，deviceName: " + wifiP2pDevice.deviceName)
        wifiP2pManager.connect(wifiP2pChannel, wifiP2pConfig,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    log("connect onSuccess")
                }

                override fun onFailure(reason: Int) {
                    showToast("Connection failed $reason")
                    dismissLoadingDialog()
                }
            })
    }

    private fun disconnect() {
        wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                log("cancelConnect onFailure:$reasonCode")
            }

            override fun onSuccess() {
                log("cancelConnect onSuccess")
                tvConnectionStatus.text = null
                btnDisconnect.isEnabled = false
                btnChooseFile.isEnabled = false
            }
        })
        wifiP2pManager.removeGroup(wifiP2pChannel, null)
    }

    private fun log(log: String) {
        tvLog.append(log)
        tvLog.append("\n\n")
    }

    private fun clearLog() {
        tvLog.text = ""
    }

    private fun startRepeatingCall(ipAddress: String, fileUri: Uri) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Call your function here

                if(fileSenderViewModel.filesent!=true) {

                fileSenderViewModel.send(ipAddress = ipAddress, fileUri = fileUri)
                    handler.postDelayed(this, interval.toLong())
                }


            }
        }, interval.toLong())
    }

}