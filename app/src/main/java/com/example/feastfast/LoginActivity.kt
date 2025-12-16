package com.example.feastfast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.feastfast.databinding.ActivityLoginBinding
import com.example.feastfast.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private var userName: String? = null
    private lateinit var email: String
    private lateinit var password: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Google Sign-In Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Email Login
        binding.loginbutton.setOnClickListener {
            email = binding.editTextTextEmailAddress.text.toString().trim()
            password = binding.editTextTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser()
            }
        }

        // Go to Sign Up
        binding.donthavebutton.setOnClickListener {
            startActivity(Intent(this, SignActivity::class.java))
        }

        // Google Login
        binding.googleButton1.setOnClickListener {
            launcher.launch(googleSignInClient.signInIntent)
        }

        // Edge Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Google Sign-In Launcher
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            if (task.isSuccessful) {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {

                        saveGoogleUser(auth.currentUser)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Email Login
    private fun loginUser() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveEmailUser()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Save Email Login User (SAFELY)
    private fun saveEmailUser() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userRef = database.child("users").child(userId)

        // Check if user exists before writing to avoid deleting History/Cart
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Only create a new node if the user DOES NOT exist
                    val userModel = UserModel(userName, email, password)
                    userRef.setValue(userModel)
                }
                // If snapshot.exists() is true, we do nothing, preserving Cart and History
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    // Save Google Login User (SAFELY)
    private fun saveGoogleUser(user: FirebaseUser?) {
        user?.let {
            val userId = it.uid
            val userRef = database.child("users").child(userId)

            // Check if user exists before writing to avoid deleting History/Cart
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // Only create a new node if the user DOES NOT exist
                        val userModel = UserModel(
                            name = it.displayName,
                            email = it.email,
                            password = null
                        )
                        userRef.setValue(userModel)
                    }
                    // If snapshot.exists() is true, we do nothing.
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            updateUI(currentUser)
        }
    }

    // Go to Main Screen
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
