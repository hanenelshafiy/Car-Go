// package com.example.car_go
package com.example.car_go

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.car_go.data.CarGoDatabase
import com.example.car_go.data.Car
import com.example.car_go.ui.theme.CARGOTheme
import androidx.room.Room
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            CarGoDatabase::class.java,
            "car-go-db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // set database before Compose content
        CarRepository.database = db

        setContent {
            CARGOTheme {
                Surface {
                    CarGoApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarGoApp(viewModel: CarViewModel = viewModel(), modifier: Modifier = Modifier) {
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartItemCount = cartItems.sumOf { it.quantity }
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("CAR-GO: Find Your Ride") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            actions = {
                IconButton(onClick = {
                    context.startActivity(Intent(context, CartActivity::class.java))
                }) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge { Text(cartItemCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        )

        CarListScreen(
            cars = cars,
            onCarClick = { carId ->
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("CAR_ID", carId)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CarListScreen(cars: List<Car>, onCarClick: (String) -> Unit, modifier: Modifier = Modifier) {
    if (cars.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cars) { car ->
            CarListItem(car = car, onClick = { onCarClick(car.id) })
        }
    }
}

@Composable
fun CarListItem(car: Car, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = "${car.make} ${car.model} image",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text("${car.make} ${car.model} (${car.year})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$${String.format("%,.2f", car.price)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                if (car.isElectric) {
                    Text("⚡ Electric Vehicle", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                }
            }
        }
    }
}