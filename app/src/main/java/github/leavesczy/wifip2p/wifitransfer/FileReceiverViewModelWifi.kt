package github.leavesczy.wifip2p.wifitransfer

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import github.leavesczy.wifip2p.Constants
import github.leavesczy.wifip2p.modelwifi.FileTransfer
import github.leavesczy.wifip2p.modelwifi.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket

class FileReceiverViewModelWifi(context: Application):
AndroidViewModel(context){

    private val _viewState = MutableSharedFlow<ViewState>()

    val viewState: SharedFlow<ViewState> = _viewState

    private val _log = MutableSharedFlow<String>()
    val fileReceiverActivityWifi=FileReceiverActivityWifi()




    val log: SharedFlow<String> = _log

    private var job: Job? = null

    fun startListener() {
        if (job != null) {
            return
        }
        job = viewModelScope.launch(context = Dispatchers.IO) {
            _viewState.emit(value = ViewState.Idle)

            var serverSocket: ServerSocket? = null
            var clientInputStream: InputStream? = null
            var objectInputStream: ObjectInputStream? = null
            var fileOutputStream: FileOutputStream? = null

            try {
                _viewState.emit(value = ViewState.Connecting)
                _log.emit(value = "\n" +
                        "turn on Socket")

                serverSocket = ServerSocket()
                serverSocket.bind(InetSocketAddress(Constants.PORT2))
                serverSocket.reuseAddress = true
                serverSocket.soTimeout = 15000

                _log.emit(value = "socket accept，\n" +
                        "Disconnect if unsuccessful within fifteen seconds")

                val client = serverSocket.accept()

                _viewState.emit(value = ViewState.Receiving)

                clientInputStream = client.getInputStream()
                objectInputStream = ObjectInputStream(clientInputStream)
                val fileTransfer = objectInputStream.readObject() as FileTransfer
                val file = File("/storage/emulated/0/Download", fileTransfer.fileName)

                _log.emit(value = "\n" +
                        "The connection is successful, the file to be received: $fileTransfer")
                _log.emit(value = "The file will be saved to: $file")
                _log.emit(value = "start file transfer")

                fileOutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024 * 512)
                while (true) {
                    val length = clientInputStream.read(buffer)
                    if (length > 0) {
                        fileOutputStream.write(buffer, 0, length)
                    } else {
                        break
                    }
                    _log.emit(value = "\n" +
                            "transferring files，length : $length")
                }
                _viewState.emit(value = ViewState.Success(file = file))
                _log.emit(value = "File received successfully")

                fileReceiverActivityWifi.showTimerBasedToast(5000,15000,"null")




            } catch (e: Throwable) {
                _log.emit(value = "abnormal: " + e.message)
                _viewState.emit(value = ViewState.Failed(throwable = e))

            } finally {
                serverSocket?.close()
                clientInputStream?.close()
                objectInputStream?.close()
                fileOutputStream?.close()
            }
        }
        job?.invokeOnCompletion {
            job = null
        }
    }

    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, "FileTransfer")
        cacheDir.mkdirs()
        return cacheDir
    }



}