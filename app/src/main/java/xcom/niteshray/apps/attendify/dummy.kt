package xcom.niteshray.apps.attendify
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*

object DummyDataGenerator {

    private val db = FirebaseFirestore.getInstance()

    fun generateDummyData() {
        addDepartments()

    }

    private fun addDepartments() {
        val departments = listOf(
            mapOf(
                "name" to "Computer",
                "classes" to mapOf(
                    "SE" to mapOf(
                        "name" to "SE",
                        "subjects" to listOf("OS", "DBMS", "CN"),
                        "facultyIds" to listOf("FAC001", "FAC002"),
                        "studentIds" to listOf("STU001", "STU002")
                    ),
                    "TE" to mapOf(
                        "name" to "TE",
                        "subjects" to listOf("AI", "ML"),
                        "facultyIds" to listOf("FAC002"),
                        "studentIds" to listOf("STU003")
                    )
                )
            ),
            mapOf(
                "name" to "Electronics",
                "classes" to mapOf(
                    "FY" to mapOf(
                        "name" to "FY",
                        "subjects" to listOf("Basic Electronics", "Physics"),
                        "facultyIds" to listOf("FAC003"),
                        "studentIds" to listOf("STU004")
                    )
                )
            )
        )

        departments.forEach { dept ->
            val deptId = dept["name"] as String
            db.collection("departments").document(deptId).set(mapOf("name" to deptId))
                .addOnSuccessListener {
                    // Add classes as subcollection
                    val classes = dept["classes"] as Map<String, Map<String, Any>>
                    classes.forEach { (classId, classData) ->
                        db.collection("departments").document(deptId)
                            .collection("classes").document(classId)
                            .set(classData)
                            .addOnSuccessListener { println("$classId added to $deptId") }
                    }
                }
        }
    }

    private fun addAttendanceRecords() {
        val attendanceRecords = listOf(
            mapOf(
                "deptId" to "Computer",
                "classId" to "SE",
                "subject" to "OS",
                "facultyId" to "FAC001",
                "date" to Date(), // Current date
                "qrCode" to "QR_${UUID.randomUUID()}",
                "students" to mapOf(
                    "STU001" to mapOf(
                        "status" to "present",
                        "timestamp" to Date(),
                        "location" to GeoPoint(18.5, 73.8),
                        "faceVerified" to true
                    ),
                    "STU002" to mapOf(
                        "status" to "absent",
                        "timestamp" to Date(),
                        "location" to GeoPoint(0.0, 0.0), // Absent, no valid location
                        "faceVerified" to false
                    )
                )
            ),
            mapOf(
                "deptId" to "Computer",
                "classId" to "TE",
                "subject" to "AI",
                "facultyId" to "FAC002",
                "date" to Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), // Yesterday
                "qrCode" to "QR_${UUID.randomUUID()}",
                "students" to mapOf(
                    "STU003" to mapOf(
                        "status" to "present",
                        "timestamp" to Date(),
                        "location" to GeoPoint(18.5, 73.8),
                        "faceVerified" to true
                    )
                )
            )
        )

        attendanceRecords.forEach { record ->
            val sessionId = "session_${UUID.randomUUID()}"
            db.collection("attendance").document(sessionId)
                .set(
                    mapOf(
                        "deptId" to record["deptId"],
                        "classId" to record["classId"],
                        "subject" to record["subject"],
                        "facultyId" to record["facultyId"],
                        "date" to record["date"],
                        "qrCode" to record["qrCode"]
                    )
                )
                .addOnSuccessListener {
                    val students = record["students"] as Map<String, Map<String, Any>>
                    students.forEach { (studentId, studentData) ->
                        db.collection("attendance").document(sessionId)
                            .collection("students").document(studentId)
                            .set(studentData)
                            .addOnSuccessListener { println("$studentId attendance added") }
                    }
                }
        }
    }
}

// Call this function from your MainActivity or wherever you want to populate data
fun main() {
    DummyDataGenerator.generateDummyData()
}