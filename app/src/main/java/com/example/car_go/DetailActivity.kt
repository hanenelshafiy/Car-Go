// package com.example.car_go
package com.example.car_go

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.car_go.ui.theme.CARGOTheme
import coil.compose.AsyncImage

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val carId = intent.getStringExtra("CAR_ID") ?: run {
            finish()
            return
        }
        setContent {
            CARGOTheme {
                CarDetailScreen(carId = carId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(carId: String, vm: CarViewModel = viewModel()) {
    val carState by vm.getCarById(carId).collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(carState?.model ?: "Car Details") },
            navigationIcon = {
                IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        )
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val car = carState) {
                null -> Text("Car not found or loading...", Modifier.align(Alignment.Center).padding(16.dp))
                else -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${car.make} ${car.model}", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Year: ${car.year}", style = MaterialTheme.typography.titleMedium)
                        Text("Price: $${String.format("%,.2f", car.price)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        AsyncImage(model = car.imageUrl, contentDescription = "Car image", modifier = Modifier.fillMaxWidth().height(180.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(car.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.addToCart(carId) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Add to Cart")
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}