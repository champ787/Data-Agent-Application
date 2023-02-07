package github.leavesczy.wifip2p.wifitransfer

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
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
import kotlinx.coroutines.withContext
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

class FileSenderViewModelWifi(context: Application) :
    AndroidViewModel(context) {

        private val _viewState = MutableSharedFlow<ViewState>()

        val viewState: SharedFlow<ViewState> = _viewState

        private val _log = MutableSharedFlow<String>()

        val log: SharedFlow<String> = _log

        private var job: Job? = null

        fun send(ipAddress: String, fileUri: Uri) {
            if (job != null) {
                return
            }
            job = viewModelScope.launch {
                withContext(context = Dispatchers.IO) {
                    _viewState.emit(value = ViewState.Idle)

                    var socket: Socket? = null
                    var outputStream: OutputStream? = null
                    var objectOutputStream: ObjectOutputStream? = null
                    var fileInputStream: FileInputStream? = null
                    try {
                        val cacheFile =
                            saveFileToCacheDir(context = getApplication(), fileUri = fileUri)
                        val fileTransfer = FileTransfer(fileName = cacheFile.name)

                        _viewState.emit(value = ViewState.Connecting)
                        _log.emit(value = "\n" + "File to Send: $fileTransfer")
                        _log.emit(value = "\n" + "Turn on Socket")

                        socket = Socket()
                        socket.bind(null)

                        _log.emit(value = "Socket Connect，Give up if the connection is not successful within 60 seconds")

                        socket.connect(InetSocketAddress(ipAddress, Constants.PORT2), 17000)

                        _viewState.emit(value = ViewState.Receiving)
                        _log.emit(value = "\n" + "The connection is successful, and the file transfer starts")

                        outputStream = socket.getOutputStream()
                        objectOutputStream = ObjectOutputStream(outputStream)
                        objectOutputStream.writeObject(fileTransfer)
                        fileInputStream = FileInputStream(cacheFile)
                        val buffer = ByteArray(1024 * 512)
                        var length: Int
                        while (true) {
                            length = fileInputStream.read(buffer)
                            if (length > 0) {
                                outputStream.write(buffer, 0, length)
                            } else {
                                break
                            }
                            _log.emit(value = "\n" +
                                    "Transferring files，length : $length")
                        }
                        _log.emit(value = "File sent successfully")
                        _viewState.emit(value = ViewState.Success(file = cacheFile))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        _log.emit(value = "\n" +
                                "abnormal: " + e.message)
                        _viewState.emit(value = ViewState.Failed(throwable = e))
                    } finally {
                        fileInputStream?.close()
                        outputStream?.close()
                        objectOutputStream?.close()
                        socket?.close()
                    }
                }
            }
            job?.invokeOnCompletion {
                job = null
            }
        }

        private suspend fun saveFileToCacheDir(context: Context, fileUri: Uri): File {
            return withContext(context = Dispatchers.IO) {
                val documentFile = DocumentFile.fromSingleUri(context, fileUri)
                    ?: throw NullPointerException("fileName for given input Uri is null")
                val fileName = documentFile.name
                val outputFile = File(
                    context.cacheDir, fileName)
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                outputFile.createNewFile()
                val outputFileUri = Uri.fromFile(outputFile)
                copyFile(context, fileUri, outputFileUri)
                return@withContext outputFile
            }
        }

        private suspend fun copyFile(context: Context, inputUri: Uri, outputUri: Uri) {
            withContext(context = Dispatchers.IO) {
                val inputStream = context.contentResolver.openInputStream(inputUri)
                    ?: throw NullPointerException("InputStream for given input Uri is null")
                val outputStream = FileOutputStream(outputUri.toFile())
                val buffer = ByteArray(1024)
                var length: Int
                while (true) {
                    length = inputStream.read(buffer)
                    if (length > 0) {
                        outputStream.write(buffer, 0, length)
                    } else {
                        break
                    }
                }
                inputStream.close()
                outputStream.close()
            }

        }
    }
