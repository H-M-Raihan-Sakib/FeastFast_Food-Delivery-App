package com.example.feastfast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.feastfast.databinding.ActivitySignBinding
import com.example.feastfast.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Firebase Auth + Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Google Sign-In Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Create Account button
        binding.createAccountButton.setOnClickListener {
            username = binding.userName.text.toString().trim()
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }

        // Already have account → Login
        binding.alreadyhavebutton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Google Sign-In
        binding.googleButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            launcher.launch(intent)
        }

        // Insets (UI padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Google Login Result
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    val account = task.result
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
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

    // Email-Password Signup
    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserData()
                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Failed", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("SIGNUP", "Error: ", task.exception)
                }
            }
    }

    private fun saveUserData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val user = UserModel(
            name = binding.userName.text.toString().trim(),
            email = binding.emailAddress.text.toString().trim(),
            password = binding.password.text.toString().trim()
        )

        database.child("users").child(userId).setValue(user)
    }
}
