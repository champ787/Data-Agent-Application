package github.leavesczy.wifip2p.wifitransfer

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import github.leavesczy.wifip2p.BaseActivityWifi
import github.leavesczy.wifip2p.R
import github.leavesczy.wifip2p.modelwifi.ViewState
import kotlinx.coroutines.launch


class FileSenderActivityWifi : BaseActivityWifi() {



    private val fileSenderViewModel by viewModels<FileSenderViewModelWifi>()

    private val getContentLaunch = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            fileSenderViewModel.send(
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sender_wifi)
        supportActionBar?.title = "\n" +
                "File Sender"
        btnChooseFile.setOnClickListener {
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

}
