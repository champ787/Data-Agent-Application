package github.leavesczy.wifip2p.wifitransfer

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import github.leavesczy.wifip2p.BaseActivityWifi
import github.leavesczy.wifip2p.R
import github.leavesczy.wifip2p.modelwifi.ViewState
import kotlinx.coroutines.launch

class FileReceiverActivityWifi : BaseActivityWifi() {
    private val handler = Handler()
    private val interval = 15000 // 15 second
    private val fileReceiverViewModel by viewModels<FileReceiverViewModelWifi>()

    private val tvState by lazy {
        findViewById<TextView>(R.id.tvState)
    }

    private val btnStartReceive by lazy {
        findViewById<Button>(R.id.btnStartReceive)
    }

//    private val ivImage by lazy {
//        findViewById<ImageView>(R.id.ivImage)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_receiver_wifi)
        supportActionBar?.title = "\n" +
                "File Receiver"
        btnStartReceive.setOnClickListener {
//            tvState.text = ""
//            ivImage.load(data = null)

                       showTimerBasedToast(5000,30000,"null")
            startRepeatingCall()
        }
        initEvent()

    }

    private fun initEvent() {
        lifecycleScope.launch {
            fileReceiverViewModel.viewState.collect {
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
//                        ivImage.load(data = it.file)
                    }

                    is ViewState.Failed -> {
                        dismissLoadingDialog()
                    }
                }
            }
        }
        lifecycleScope.launch {
            fileReceiverViewModel.log.collect {
                tvState.append(it)
                tvState.append("\n\n")
            }
        }

    }
    private fun startRepeatingCall() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Call your function here
                fileReceiverViewModel.startListener()
                handler.postDelayed(this, interval.toLong())
            }
        }, interval.toLong())
    }


//   public fun showTimerBasedToast(time: Long,ipaddress:String) {
//       val handler = Handler()
//         handler.postDelayed({
//         Toast.makeText(this, "Alert: Switch on HotSpot for: "+ipaddress, Toast.LENGTH_LONG).show()
//           }, time)
//      }


     fun showTimerBasedToast(time: Long, delay: Long,ipaddress:String) {
         val handler = Handler()
         handler.postDelayed({
             Toast.makeText(this, "Alert: Turm on device hotspot: "+ipaddress, Toast.LENGTH_SHORT).show()
         }, delay + time)}

     }