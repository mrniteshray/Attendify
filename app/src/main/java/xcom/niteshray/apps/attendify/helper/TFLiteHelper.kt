package xcom.niteshray.apps.attendify.helper

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class TFLiteHelper(private val context: Context) {
    private var interpreter: Interpreter? = null

    init {
        interpreter = Interpreter(loadModelFile())
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("mobile_face_net.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun getInterpreter(): Interpreter {
        return interpreter!!
    }
}