package com.example.car_go

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.car_go.ui.theme.CARGOTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : ComponentActivity() {

    private val viewModel: CarViewModel by viewModels { CarViewModel.Factory }
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContent {
            var name by remember { mutableStateOf("Loading...") }
            var email by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var country by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                firestore.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            name = document.getString("name") ?: "No Name"
                            email = document.getString("email") ?: ""
                            phone = document.getString("phone") ?: ""
                            country = document.getString("country") ?: ""
                        } else {
                            Toast.makeText(this@ProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ProfileActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
            }

            CARGOTheme {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProfileScreen(
                        name = name,
                        email = email,
                        phone = phone,
                        country = country,
                        onLogoutClick = {
                            auth.signOut()
                            Toast.makeText(this@ProfileActivity, "Logged out", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ProfileActivity, AuthActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        },
                        onBackClick = { onBackPressedDispatcher.onBackPressed() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String?,
    email: String?,
    phone: String?,
    country: String?,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CarViewModel
) {
    val bookings by viewModel.getBookings().collectAsStateWithLifecycle(initialValue = emptyList())
    val cars by viewModel.cars.collectAsStateWithLifecycle(initialValue = emptyList())

    fun getCarDetails(carId: String): Car? {
        return cars.find { it.id == carId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    name?.let { Text("Name: $it", fontSize = 16.sp) }
                    email?.let { Text("Email: $it", fontSize = 16.sp) }
                    phone?.let { Text("Phone Number: $it", fontSize = 16.sp) }
                    country?.let { Text("Country: $it", fontSize = 16.sp) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("My Bookings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(bookings) { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Booking ID: ${booking.id}", fontWeight = FontWeight.Bold)
                            Text("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(booking.timestamp.toDate())}")
                            Text("Total: $${String.format("%.2f", booking.totalPrice)}")
                            Spacer(modifier = Modifier.height(8.dp))
                            booking.items.forEach { cartItem ->
                                val car = getCarDetails(cartItem.carId)
                                car?.let {
                                    Text("- ${it.make} ${it.model} (x${cartItem.quantity})")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
