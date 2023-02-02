package github.leavesczy.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import github.leavesczy.wifip2p.receiver.FileReceiverActivity
import github.leavesczy.wifip2p.sender.FileSenderActivity
import github.leavesczy.wifip2p.wifitransfer.FileReceiverActivityWifi
import github.leavesczy.wifip2p.wifitransfer.FileSenderActivityWifi

/**
 * @Author: CZY
 * @Date: 2022/9/28 14:24
 * @Desc:
 */
class MainActivity : BaseActivity() {

    private val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val requestPermissionLaunch = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        if (it.all { it.value }) {
            showToast("Full access has been granted")
        } else {
            onPermissionDenied()
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        findViewById<View>(R.id.btnCheckPermission).setOnClickListener {
            requestPermissionLaunch.launch(requestedPermissions)
        }
        findViewById<View>(R.id.btnSender).setOnClickListener {
            if (allPermissionGranted()) {
                startActivity(FileSenderActivity::class.java)
            } else {
                onPermissionDenied()
            }
        }
        findViewById<View>(R.id.btnReceiver).setOnClickListener {
            if (allPermissionGranted()) {
                startActivity(FileReceiverActivity::class.java)
            } else {
                onPermissionDenied()
            }
        }
        findViewById<View>(R.id.btnProgress).setOnClickListener {
            if (allPermissionGranted()) {
                startActivity(Monitor_Progress::class.java)
            } else {
                onPermissionDenied()
            }
        }

        findViewById<View>(R.id.btnSendProgress).setOnClickListener {
            startActivity(FileSenderActivityWifi::class.java)
        }


        findViewById<View>(R.id.btnReceiveProgress).setOnClickListener {
            startActivity(FileReceiverActivityWifi::class.java)
        }

    }

    private fun onPermissionDenied() {
        showToast("Missing permission, please grant permission first")
    }

    private fun allPermissionGranted(): Boolean {
        requestedPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}