package xcom.niteshray.apps.attendify.helper
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceNetModel(context: Context) {
    private val tflite: Interpreter

    init {
        val modelFile = loadModelFile(context, "mobile_face_net.tflite")
        tflite = Interpreter(modelFile)
    }

    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun getFaceEmbedding(bitmap: Bitmap, callback: (FloatArray?) -> Unit) {
        val inputImageSize = 112 // MobileFaceNet expects 112x112

        // Step 1: Detect and crop face asynchronously
        detectAndCropFace(bitmap) { croppedBitmap ->
            if (croppedBitmap == null) {
                Log.e("FaceNet", "No face detected")
                callback(null)
                return@detectAndCropFace
            }

            // Step 2: Resize to 112x112
            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, inputImageSize, inputImageSize, true)

            // Step 3: Convert to buffer
            val inputBuffer = convertBitmapToBuffer(resizedBitmap)

            // Debug tensor size
            val inputTensor = tflite.getInputTensor(0)
            Log.d("TensorFlow", "Expected Tensor Size: ${inputTensor.numBytes()}, Input Buffer Size: ${inputBuffer.capacity()}")

            val outputArray = Array(1) { FloatArray(192) } // MobileFaceNet outputs 192-dim embeddings
            tflite.run(inputBuffer, outputArray)

            Log.d("Embedding", "Generated Embedding: ${outputArray[0].joinToString()}")
            callback(outputArray[0])
        }
    }

    fun isFaceMatch(embedding1: FloatArray, embedding2: FloatArray): Boolean {
        var distance = 0f
        for (i in embedding1.indices) {
            distance += (embedding1[i] - embedding2[i]) * (embedding1[i] - embedding2[i])
        }
        distance = sqrt(distance)
        Log.d("FaceMatch", "Euclidean Distance: $distance")
        return distance < 0.6f // Tune this threshold after testing
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val inputImageSize = 112
        val imgData = ByteBuffer.allocateDirect(inputImageSize * inputImageSize * 3 * 4) // 112x112x3x4 bytes
        imgData.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputImageSize * inputImageSize)
        bitmap.getPixels(intValues, 0, inputImageSize, 0, 0, inputImageSize, inputImageSize)

        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f // Normalize to [-1, 1]
            val g = ((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f
            val b = ((pixelValue and 0xFF) - 127.5f) / 127.5f
            imgData.putFloat(r)
            imgData.putFloat(g)
            imgData.putFloat(b)
        }
        return imgData
    }

    private fun detectAndCropFace(bitmap: Bitmap, callback: (Bitmap?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient(FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.1f) // Lowered to detect smaller faces
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .build())

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0] // Take the first detected face
                    val box = face.boundingBox

                    // Ensure the crop stays within bitmap bounds
                    val left = max(0, box.left)
                    val top = max(0, box.top)
                    val width = min(box.width(), bitmap.width - left)
                    val height = min(box.height(), bitmap.height - top)

                    if (width > 0 && height > 0) {
                        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
                        Log.d("FaceDetection", "Face detected and cropped: ${croppedBitmap.width}x${croppedBitmap.height}")
                        callback(croppedBitmap)
                    } else {
                        Log.e("FaceDetection", "Invalid crop dimensions: $width x $height")
                        callback(null)
                    }
                } else {
                    Log.e("FaceDetection", "No faces found in image")
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e("FaceDetection", "Failed to process image: ${it.message}")
                callback(null)
            }
    }
}

//class FaceNetModel(context: Context) {
//    private val tflite: Interpreter
//
//    init {
//        val modelFile = loadModelFile(context, "mobile_face_net.tflite")
//        tflite = Interpreter(modelFile)
//    }
//
//    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
//        val fileDescriptor = context.assets.openFd(modelName)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
//    }
//
//    fun getFaceEmbedding(bitmap: Bitmap): FloatArray {
//        val inputImageSize = 112 // MobileFaceNet typically uses 112x112
//
//        // Resize to 112x112
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)
//
//        val inputBuffer = convertBitmapToBuffer(resizedBitmap)
//
//        // Debug input tensor size
//        val inputTensor = tflite.getInputTensor(0)
//        val expectedSize = inputTensor.numBytes()
//        Log.d("TensorFlow", "Expected Tensor Size: $expectedSize, Input Buffer Size: ${inputBuffer.capacity()}")
//
//        val outputArray = Array(1) { FloatArray(192) } // Assuming output is 192-dim embedding
//        tflite.run(inputBuffer, outputArray)
//
//        return outputArray[0]
//    }
//
//    fun isFaceMatch(embedding1: FloatArray, embedding2: FloatArray): Boolean {
//        var distance = 0f
//        for (i in embedding1.indices) {
//            distance += (embedding1[i] - embedding2[i]) * (embedding1[i] - embedding2[i])
//        }
//        distance = sqrt(distance)
//        Log.d("FaceMatch", "Distance: $distance")
//        return distance < 0.6f // Adjust threshold based on testing
//    }
//
//    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
//        val inputImageSize = 112
//        val imgData = ByteBuffer.allocateDirect(inputImageSize * inputImageSize * 3 * 4) // 112x112x3x4 bytes (float32)
//        imgData.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(inputImageSize * inputImageSize)
//        bitmap.getPixels(intValues, 0, inputImageSize, 0, 0, inputImageSize, inputImageSize)
//
//        for (pixelValue in intValues) {
//            val r = ((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f // Normalize to [-1, 1]
//            val g = ((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f
//            val b = ((pixelValue and 0xFF) - 127.5f) / 127.5f
//            imgData.putFloat(r)
//            imgData.putFloat(g)
//            imgData.putFloat(b)
//        }
//        return imgData
//    }
//}

