package xcom.niteshray.apps.attendify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.attendify.Model.user
import xcom.niteshray.apps.attendify.databinding.ActivityLoginBinding

class login_Activity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private val mauth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (mauth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email == "Admin@gmail.com"&&password=="admin") {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
            }
            if (email.isNotEmpty() && password.isNotEmpty()) {
                    mauth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {

                    }
                }
            }
        }

    fun signUpUser(email: String, password: String, user: user) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val newUser = user.copy(userId = userId)
                    addUserToFirestore(newUser)
                } else {
                    Log.e("Auth", "Sign Up Failed", task.exception)
                }
            }
    }

    fun addUserToFirestore(user: user) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user", e)
            }
    }
}
