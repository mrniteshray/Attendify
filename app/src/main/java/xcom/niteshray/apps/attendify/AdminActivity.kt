package xcom.niteshray.apps.attendify

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.net.Uri
import android.graphics.BitmapFactory
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.attendify.Model.user
import xcom.niteshray.apps.attendify.databinding.ActivityAdminBinding
import xcom.niteshray.apps.attendify.databinding.DialogAddUserBinding
import xcom.niteshray.apps.attendify.helper.FaceNetModel
import java.util.concurrent.Executors
import android.Manifest
import android.os.Environment
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FieldValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date


class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val defaultClasses = listOf("FE", "SE", "TE", "BE")
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var faceNetModel: FaceNetModel
    private val CAMERA_PERMISSION_CODE = 100
    private var savedEmbedding: FloatArray? = null
    private lateinit var photoFile: File
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        faceNetModel = FaceNetModel(this)

        binding.addUserButton.setOnClickListener {
            showAddUserDialog()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_CODE)
        }
    }

    private fun showAddUserDialog() {
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add User")
            .setView(dialogBinding.root)
            .create()

        // Setup role spinner
        val roles = arrayOf("student", "faculty")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.roleSpinner.adapter = roleAdapter

        // Load departments for department spinner
        db.collection("departments").get().addOnSuccessListener { documents ->
            val departments = documents.map { it.getString("name") ?: "" }
            val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.departmentSpinner.adapter = departmentAdapter

            val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, defaultClasses)
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.classSpinner.adapter = classAdapter
        }

        // Capture face button
        dialogBinding.captureFaceButton.setOnClickListener {
            openCamera()
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add") { _, _ ->
            val name = dialogBinding.nameInput.text.toString()
            val email = dialogBinding.emailInput.text.toString()
            val collegeId = dialogBinding.collegeIdInput.text.toString()
            val role = dialogBinding.roleSpinner.selectedItem.toString()
            val department = dialogBinding.departmentSpinner.selectedItem.toString()
            val className = "SE"

            if (name.isNotEmpty() && email.isNotEmpty() && collegeId.isNotEmpty() && department.isNotEmpty() && savedEmbedding != null) {
                addUser(name, email, collegeId, role, department, className, savedEmbedding!!.toList())
            } else {
                Toast.makeText(this, "Please fill all fields and capture a face", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ -> }
        dialog.show()
    }

    private fun openCamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        photoFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

        val photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            Log.d("Debug", "Captured image size: ${bitmap.width}x${bitmap.height}")

            // Save bitmap for debugging
            val debugFile = File(externalCacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(debugFile))
            Log.d("Debug", "Bitmap saved to: ${debugFile.absolutePath}")

            faceNetModel.getFaceEmbedding(bitmap) { embedding ->
                savedEmbedding = embedding
                if (embedding != null) {
                    Toast.makeText(this, "Face Saved! Embedding: ${embedding.joinToString()}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to detect face", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addUser(name: String, email: String, collegeId: String, role: String,
                        departmentName: String, className: String, faceEmbedding: List<Float>) {
        auth.createUserWithEmailAndPassword(email, "Pass123")
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                val newUser = if (role == "student") {
                    user(
                        userId = userId,
                        role = role,
                        name = name,
                        email = email,
                        collegeId = collegeId,
                        department = departmentName,
                        faceEmbedding = faceEmbedding,
                        studentClass = className,
                        subjects = emptyList()
                    )
                } else {
                    user(
                        userId = userId,
                        role = role,
                        name = name,
                        email = email,
                        collegeId = collegeId,
                        department = departmentName,
                        faceEmbedding = faceEmbedding,
                        studentClass = "",
                        subjects = emptyList()
                    )
                }

                db.collection("users").document(userId)
                    .set(newUser)
                    .addOnSuccessListener {
                        updateDepartmentClassWithNewUser(departmentName, className, userId, role)
                        Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add user data", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user authentication", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDepartmentClassWithNewUser(departmentName: String, className: String, userId: String, role: String) {
        if (departmentName.isEmpty() || (role == "student" && className.isEmpty())) return

        val classRef = db.collection("departments")
            .document(departmentName)
            .collection("classes")
            .document(className)

        classRef.update("studentIds", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                println("Student ID successfully added to class!")
            }
            .addOnFailureListener { e ->
                println("Error adding student ID to class: $e")
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // Permissions granted
        } else {
            Toast.makeText(this, "Camera or storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
