package xcom.niteshray.apps.attendify.Faculty

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import okhttp3.Credentials
import retrofit2.Call
import xcom.niteshray.apps.attendify.Model.AttendanceSession
import xcom.niteshray.apps.attendify.api.RetrofitClient
import xcom.niteshray.apps.attendify.databinding.FragmentSessionBinding

class SessionFragment : Fragment() {
    private lateinit var _binding : FragmentSessionBinding
    private val binding get() = _binding!!

    private val currentUseruid = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val args: SessionFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attendanceSession = args.Session
        generateQRCode(attendanceSession.qrCode){
            startSession(attendanceSession)
        }

        binding.btnEndSession.setOnClickListener {
            sendWhatsAppAlert()
            endSession(attendanceSession)
        }
    }

    fun endSession(attendanceSession: AttendanceSession) {
        db.collection("Attendance")
            .document(attendanceSession.sessionId)
            .update("sessionEnded", true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Session Ended", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
                }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    fun sendWhatsAppAlert() {
        val accountSid = "AC8416e7df9e7ae8bd8a1a6c08308c9832"
        val authToken = "879c5096660d5ee1d4dfc7c6fda3ca1c"
        val fromWhatsApp = "whatsapp:+14155238886"
        val toWhatsApp = "whatsapp:+918669492717"
        val alertMessage = "🚨  Alert: Your Ward is absent today"

        // ✅ Generate Auth Header
        val authHeader = Credentials.basic(accountSid, authToken)

        val twilioApi = RetrofitClient.getTwilioService()
        val call = twilioApi.sendWhatsAppMessage(accountSid, authHeader, toWhatsApp, fromWhatsApp, alertMessage)

        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("WhatsApp", "Message sent successfully")
                } else {
                    Log.e("WhatsApp", "Failed to send message: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("❌ Error: ${t.message}")
            }
        })
    }


    fun startSession(attendanceSession: AttendanceSession){
        db.collection("Attendance")
            .document(attendanceSession.sessionId)
            .set(attendanceSession)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Session Started", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateQRCode(text: String,onSuccess: () -> Unit) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitMatrix: BitMatrix = barcodeEncoder.encode(text, BarcodeFormat.QR_CODE, 400, 400)
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.SessionQr.setImageBitmap(bitmap)
            onSuccess()
        } catch (e: WriterException) {
            Toast.makeText(requireContext(),e.toString(),Toast.LENGTH_SHORT).show()
        }
    }

}