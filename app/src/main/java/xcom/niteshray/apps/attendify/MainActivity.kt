package xcom.niteshray.apps.attendify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.attendify.Model.user
import xcom.niteshray.apps.attendify.databinding.ActivityMainBinding
import xcom.niteshray.apps.attendify.helper.LoadingFragmentDirections

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    private val db = FirebaseFirestore.getInstance()
    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    private var currentuserRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, login_Activity::class.java))
            finish()
        }

        db.collection("users").get().addOnSuccessListener {
            for (document in it.documents) {
                val user = document.toObject(user::class.java)
                if (user != null && user.userId == currentUserID) {
                    currentuserRole = user.role
                    if (user.role == "student") {
                        val action= LoadingFragmentDirections.actionLoadingFragmentToHomeFragment(user)
                        navController.navigate(action)
                    } else if (user.role == "faculty") {
                        navController.navigate(R.id.facultyFragment)
                    }
                }
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

//        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
//        toggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

    }
}