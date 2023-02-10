package github.leavesczy.wifip2p.wifitransfer

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import github.leavesczy.wifip2p.BaseActivityWifi
import github.leavesczy.wifip2p.R
import github.leavesczy.wifip2p.modelwifi.ViewState

import kotlinx.coroutines.launch



class FileSenderActivityWifi : BaseActivityWifi() {

    private val handler = Handler()
    private val interval = 5000 // 5 second

    private val fileSenderViewModel by viewModels<FileSenderViewModelWifi>()

    private val getContentLaunch = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            startRepeatingCall(
                ipAddress = getHotspotIpAddress(
                    context = applicationContext
                ),
                fileUri = imageUri
            )


        }
    }

    private val btnChooseFile by lazy {
        findViewById<Button>(R.id.btnChooseFile)
    }

    private val tvState by lazy {
        findViewById<TextView>(R.id.tvState)
    }

    private val fileSenderViewModelWifi by viewModels<FileSenderViewModelWifi>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender_wifi)
        supportActionBar?.title = "\n" +
                "File Sender"
        btnChooseFile.setOnClickListener {
            fileSenderViewModelWifi.filesent=false;
            getContentLaunch.launch("*/*")
        }
        initEvent()
    }
    private fun initEvent() {
        lifecycleScope.launch {
                fileSenderViewModel.viewState.collect {
                    when (it) {
                        ViewState.Idle -> {
                            tvState.text = ""
                            dismissLoadingDialog()
                        }

                        ViewState.Connecting -> {
                            showLoadingDialog()
                        }

                        is ViewState.Receiving -> {
                            showLoadingDialog()
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
                tvState.append(it)
                tvState.append("\n\n")
            }
        }
    }

    private fun getHotspotIpAddress(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo = wifiManager?.connectionInfo
        if (wifiInfo != null) {
            val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null) {
                val address = dhcpInfo.gateway
                return ((address and 0xFF).toString() + "." + (address shr 8 and 0xFF)
                        + "." + (address shr 16 and 0xFF)
                        + "." + (address shr 24 and 0xFF))
            }
        }
        return ""
    }
    private fun startRepeatingCall(ipAddress: String, fileUri: Uri) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Call your function here
                if(fileSenderViewModelWifi.filesent!=true) {
                fileSenderViewModelWifi.send(ipAddress = ipAddress, fileUri = fileUri)
                    handler.postDelayed(this, interval.toLong())
                }
                else
                {
                    showTimerBasedToast(5000,15000,ipAddress)

                }


            }
        }, interval.toLong())
    }

    fun showTimerBasedToast(time: Long, delay: Long,ipaddress:String) {
        val handler = Handler()
        handler.postDelayed({
            Toast.makeText(this, "Alert: Connect to the Hotspot of: "+ipaddress, Toast.LENGTH_SHORT).show()
        }, delay + time)
    }

}
