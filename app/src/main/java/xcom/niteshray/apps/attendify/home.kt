package xcom.niteshray.apps.attendify

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import xcom.niteshray.apps.attendify.Model.AttendanceRecord
import xcom.niteshray.apps.attendify.Model.AttendanceSession
import xcom.niteshray.apps.attendify.databinding.FragmentHomeBinding
import xcom.niteshray.apps.attendify.helper.FaceNetModel
import xcom.niteshray.apps.attendify.helper.capAct


class home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val args: homeArgs by navArgs()
    private lateinit var faceNetModel: FaceNetModel
    private var sessionIdToMark: String? = null // Store session ID for face verification

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 2 // Unique request code for camera
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = args.user
        faceNetModel = FaceNetModel(requireContext()) // Initialize FaceNetModel
        fetchFilteredAttendanceSessions(user.department, user.studentClass)
    }

    private fun fetchFilteredAttendanceSessions(dept: String, studentClass: String) {
        db.collection("Attendance").get()
            .addOnSuccessListener { result ->
                val filteredSessions = result.documents.mapNotNull { doc ->
                    val session = doc.toObject(AttendanceSession::class.java)
                    session?.takeIf { it.deptId == dept && it.classId == studentClass }
                }
                setupRecyclerView(filteredSessions)
                fetchAttendanceAnalytics(filteredSessions)
            }
            .addOnFailureListener { e ->
                Log.e("Home", "Failed to fetch sessions: ${e.message}")
            }
    }

    private fun fetchAttendanceAnalytics(sessions: List<AttendanceSession>) {
        if (sessions.isEmpty()) {
            updateAnalyticsUI(0, 0)
            return
        }

        var completedQueries = 0
        var presentCount = 0
        val totalSessions = sessions.size

        sessions.forEach { session ->
            db.collection("Attendance")
                .document(session.sessionId)
                .collection("AttendanceRecord")
                .document(args.user.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val record = document.toObject(AttendanceRecord::class.java)
                        if (record?.status == "present") {
                            presentCount++
                        }
                    }
                    completedQueries++
                    if (completedQueries == totalSessions) {
                        updateAnalyticsUI(totalSessions, presentCount)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Home", "Error fetching attendance: ${e.message}")
                    completedQueries++
                    if (completedQueries == totalSessions) {
                        updateAnalyticsUI(totalSessions, presentCount)
                    }
                }
        }
    }
    private fun updateAnalyticsUI(totalSessions: Int, presentCount: Int) {
        val absentCount = totalSessions - presentCount
        val attendancePercentage = if (totalSessions > 0) {
            (presentCount.toFloat() / totalSessions.toFloat() * 100).toInt()
        } else {
            0
        }

        binding.apply {
            tvTotalSessions.text = totalSessions.toString()
            tvPresentSessions.text = presentCount.toString()
            tvAbsentSessions.text = absentCount.toString()
            tvAttendancePercentage.text = "$attendancePercentage%"
        }
    }

    private fun setupRecyclerView(sessions: List<AttendanceSession>) {
        binding.rvOngoingAttendance.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOngoingAttendance.adapter = AttendanceAdapter(sessions) { sessionId ->
            scanQRCode(sessionId) // Pass sessionId to QR scan
        }
    }

    private fun scanQRCode(sessionId: String) {
        sessionIdToMark = sessionId // Store session ID for later use
        IntentIntegrator.forSupportFragment(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Scan Attendance QR Code")
            setBeepEnabled(true)
            setCaptureActivity(capAct::class.java)
            initiateScan()
        }
    }

    private fun verifyFace(sessionId: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun markAttendance(sessionId: String) {
        val user = args.user
        val attendanceRecord = AttendanceRecord(user.userId, "present")
        db.collection("Attendance").document(sessionId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("Attendance")
                        .document(sessionId)
                        .collection("AttendanceRecord")
                        .document(user.userId)
                        .set(attendanceRecord)
                        .addOnSuccessListener {
                            fetchFilteredAttendanceSessions(user.department, user.studentClass)
                            Toast.makeText(requireContext(), "Attendance marked", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to mark attendance: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Session not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking session: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle QR code scan result
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result.contents != null) {
                val scannedSessionId = result.contents
                if (scannedSessionId == sessionIdToMark) { // Verify QR matches session
                    Toast.makeText(requireContext(), "QR Code verified: $scannedSessionId", Toast.LENGTH_SHORT).show()
                    verifyFace(scannedSessionId) // Proceed to face verification
                } else {
                    Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Scan Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        // Handle face capture result
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            faceNetModel.getFaceEmbedding(bitmap) { capturedEmbedding ->
                if (capturedEmbedding != null) {
                    // Fetch stored embedding from Firestore
                    db.collection("users").document(args.user.userId).get()
                        .addOnSuccessListener { document ->
                            val storedEmbedding = document.get("faceEmbedding") as? List<Double>
                            if (storedEmbedding != null) {
                                // Convert stored embedding (Double) to FloatArray
                                val storedEmbeddingFloat = storedEmbedding.map { it.toFloat() }.toFloatArray()
                                // Compare embeddings
                                if (faceNetModel.isFaceMatch(capturedEmbedding, storedEmbeddingFloat)) {
                                    Toast.makeText(requireContext(), "Face verified!", Toast.LENGTH_SHORT).show()
                                    sessionIdToMark?.let { markAttendance(it) } // Mark attendance if face matches
                                } else {
                                    Toast.makeText(requireContext(), "Face verification failed", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "No stored face embedding found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error fetching user data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Failed to detect face", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}