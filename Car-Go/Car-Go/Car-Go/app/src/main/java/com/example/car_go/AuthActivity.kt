package com.example.car_go

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.car_go.ui.screens.LoginScreen
import com.example.car_go.ui.screens.SignUpScreen
import com.example.car_go.ui.theme.CARGOTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            goToMain()
        } else {
            setContent {
                CARGOTheme {
                    AuthNavGraph()
                }
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @Composable
    fun AuthNavGraph() {
        val navController = rememberNavController()
        var isLoading by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(this@AuthActivity, "Fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnSuccessListener { 
                                        Toast.makeText(this@AuthActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
                                        goToMain() 
                                    }
                                    .addOnFailureListener { 
                                        isLoading = false
                                        Toast.makeText(this@AuthActivity, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        onSignUpClick = { navController.navigate("signup") },
                        onForgotPasswordClick = { email ->
                            if (email.isBlank()) {
                                Toast.makeText(this@AuthActivity, "Please enter your email", Toast.LENGTH_SHORT).show()
                            } else {
                                auth.sendPasswordResetEmail(email.trim())
                                    .addOnSuccessListener { 
                                        Toast.makeText(this@AuthActivity, "Password reset email sent", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { 
                                        Toast.makeText(this@AuthActivity, "Failed to send reset email: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    )
                }
                composable("signup") {
                    SignUpScreen(
                        onSignUpClick = { name, email, phoneNumber, country, password ->
                            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                                Toast.makeText(this@AuthActivity, "Fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnSuccessListener { authResult ->
                                        val userId = authResult.user?.uid
                                        val userMap = hashMapOf(
                                            "name" to name,
                                            "email" to email.trim(),
                                            "phone" to phoneNumber,
                                            "country" to country
                                        )
                                        if (userId != null) {
                                            firestore.collection("users").document(userId)
                                                .set(userMap)
                                                .addOnSuccessListener { 
                                                    Toast.makeText(this@AuthActivity, "Account Created!", Toast.LENGTH_SHORT).show()
                                                    goToMain() 
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(this@AuthActivity, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    }
                                    .addOnFailureListener { 
                                        isLoading = false
                                        Toast.makeText(this@AuthActivity, "Signup failed: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
