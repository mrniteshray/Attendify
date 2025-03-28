package xcom.niteshray.apps.attendify.Faculty

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Credentials
import retrofit2.Call
import xcom.niteshray.apps.attendify.Model.AttendanceSession
import xcom.niteshray.apps.attendify.Model.Classess
import xcom.niteshray.apps.attendify.Model.user
import xcom.niteshray.apps.attendify.R
import xcom.niteshray.apps.attendify.api.RetrofitClient
import xcom.niteshray.apps.attendify.databinding.FragmentFacultyBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.jvm.java

class FacultyFragment : Fragment() {
    private lateinit var binding: FragmentFacultyBinding
    private lateinit var subjectStatsAdapter: SubjectStatsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private lateinit var FacultyUser : user

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacultyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWelcomeMessage()

        db.collection("users").document(currentUserUid!!).get().addOnSuccessListener {
            val user = it.toObject(user::class.java)
            if (user != null) {
                FacultyUser = user
                updateWelcomeMessage(FacultyUser.name)
            }
        }
        loadTotalStudents()

        setupRecyclerView()
        loadAnalytics()
        setupClickListeners()

        Handler(Looper.getMainLooper()).postDelayed({
            loadTotalStudents()
        },4000)
    }

    private fun setupRecyclerView() {
        subjectStatsAdapter = SubjectStatsAdapter()
        binding.rvSubjectStats.apply {
            adapter = subjectStatsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadAnalytics() {
        // Load total students
        loadTotalStudents()

        // Load average attendance
        loadOverallAttendance()

        // Load subject-wise stats
        loadSubjectStats()
    }

    private fun loadTotalStudents() {
        if (::FacultyUser.isInitialized) {
            db.collection("departments")
                .document(FacultyUser.department)
                .collection("classes")
                .get()
                .addOnSuccessListener { classes ->
                    var total = 0
                    classes.forEach { doc ->
                        total += doc.toObject(Classess::class.java).studentIds.size
                    }
                    binding.tvTotalStudents.text = total.toString()
                }
        }
    }

    private fun loadOverallAttendance() {
        loadTotalStudents()
        db.collection("Attendance")
            .whereEqualTo("facultyId", currentUserUid)
            .get()
            .addOnSuccessListener { sessions ->
                var totalAttendance = 0f
                var processedSessions = 0

                sessions.forEach { session ->
                    loadSubjectStats()
                    loadTotalStudents()
                    db.collection("Attendance")
                        .document(session.id)
                        .collection("AttendanceRecord")
                        .whereEqualTo("status", "present")
                        .get()
                        .addOnSuccessListener { records ->
                            val sessionData = session.toObject(AttendanceSession::class.java)
                            val percentage = if (sessionData.studentList?.isNotEmpty() == true) {
                                (records.size().toFloat() / sessionData.studentList!!.size) * 100
                            } else 0f

                            totalAttendance += percentage
                            processedSessions++

                            if (processedSessions == sessions.size()) {
                                val average = totalAttendance / sessions.size()
                                binding.tvAverageAttendance.text = "${average.toInt()}%"
                            }
                        }
                }
            }
    }

    private fun loadSubjectStats() {
        val subjectStats = mutableListOf<SubjectStatsAdapter.SubjectStat>()

        if (::FacultyUser.isInitialized) {
            FacultyUser.subjects.forEach { subject ->
                db.collection("Attendance")
                    .whereEqualTo("facultyId", currentUserUid)
                    .whereEqualTo("subject", subject)
                    .get()
                    .addOnSuccessListener { sessions ->
                        var totalAttendance = 0f
                        var processedSessions = 0

                        if (sessions.isEmpty) {
                            subjectStats.add(SubjectStatsAdapter.SubjectStat(
                                subject = subject,
                                totalStudents = 0,
                                averageAttendance = 0f,
                                totalSessions = 0
                            ))
                            subjectStatsAdapter.submitList(subjectStats)
                            return@addOnSuccessListener
                        }

                        sessions.forEach { session ->
                            db.collection("Attendance")
                                .document(session.id)
                                .collection("AttendanceRecord")
                                .whereEqualTo("status", "present")
                                .get()
                                .addOnSuccessListener { records ->
                                    val sessionData = session.toObject(AttendanceSession::class.java)
                                    val percentage = if (sessionData.studentList?.isNotEmpty() == true) {
                                        (records.size().toFloat() / sessionData.studentList!!.size) * 100
                                    } else 0f

                                    totalAttendance += percentage
                                    processedSessions++

                                    if (processedSessions == sessions.size()) {
                                        subjectStats.add(SubjectStatsAdapter.SubjectStat(
                                            subject = subject,
                                            totalStudents = sessionData.studentList?.size ?: 0,
                                            averageAttendance = totalAttendance / sessions.size(),
                                            totalSessions = sessions.size()
                                        ))
                                        subjectStatsAdapter.submitList(subjectStats)
                                    }
                                }
                        }
                    }
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardView.setOnClickListener {
            showAttendanceDialog()
        }

//        binding.cardView2.setOnClickListener {
//            // TODO: Navigate to detailed reports
//            Toast.makeText(context, "Detailed reports coming soon!", Toast.LENGTH_SHORT).show()
//        }
    }

    fun sendWhatsAppAlert() {
        val accountSid = "AC8416e7df9e7ae8bd8a1a6c08308c9832"
        val authToken = "879c5096660d5ee1d4dfc7c6fda3ca1c"
        val fromWhatsApp = "whatsapp:+14155238886"
        val toWhatsApp = "whatsapp:+919175595765"
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

    private fun setupWelcomeMessage() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        binding.tvWelcomeTime.text = greeting
        binding.welcomeCard.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            Toast.makeText(requireContext(), "Welcome to Attendify!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWelcomeMessage(name: String) {
        binding.tvWelcomeMessage.text = "Welcome, $name!"
        binding.welcomeCard.alpha = 0f
        binding.welcomeCard.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun showAttendanceDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.session_dialog, null)
        dialog.setContentView(view)

        val autoCompleteClass = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteClass)
        val autoCompleteSubject = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteSubject)
        val btnStartAttendance = view.findViewById<MaterialButton>(R.id.btnStartAttendance)

        val classList = listOf("FE","SE","TE")
        val subjectList = FacultyUser.subjects

        val classAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classList)
        val subjectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjectList)

        autoCompleteClass.setAdapter(classAdapter)
        autoCompleteSubject.setAdapter(subjectAdapter)

        btnStartAttendance.setOnClickListener {
            val selectedClass = autoCompleteClass.text.toString().trim()
            val selectedSubject = autoCompleteSubject.text.toString().trim()

            if (selectedClass.isEmpty() || selectedSubject.isEmpty()) {
                Toast.makeText(requireContext(), "Please select class and subject", Toast.LENGTH_SHORT).show()
            } else {
                StartAttendance(selectedClass, selectedSubject)
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    fun StartAttendance(selectedClass: String, selectedSubject: String) {
        var studentlist = mutableListOf<String>()
        db.collection("departments")
            .document(FacultyUser.department)
            .collection("classes")
            .document(selectedClass).get().addOnSuccessListener {
                val classess = it.toObject(Classess::class.java)
                for(ids in classess?.studentIds!!){
                    studentlist.add(ids)
                }
                val sessionId = generateRandomSessionCode()
                val attendanceSession = AttendanceSession(
                    sessionId = sessionId,
                    deptId = FacultyUser.department,
                    classId = selectedClass,
                    date = dateFormat.toString(),
                    qrCode = sessionId,
                    subject = selectedSubject,
                    facultyId = currentUserUid!!,
                    sessionEnded = false,
                    studentList = studentlist
                )
                val action = FacultyFragmentDirections.actionFacultyFragmentToSessionFragment(attendanceSession)
                findNavController().navigate(action)
            }

    }


    private fun generateRandomSessionCode(): String {
        val randomDigits = (1000..9999).random()
        val randomText = listOf("ABC", "XYZ", "QRS", "DEF").random()
        return "$randomDigits$randomText"
    }

}
