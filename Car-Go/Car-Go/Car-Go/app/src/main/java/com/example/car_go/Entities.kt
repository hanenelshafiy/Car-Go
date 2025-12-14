package com.example.car_go

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

// Room Entity for a Car
@Entity(tableName = "cars")
data class Car(
    @PrimaryKey val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val isElectric: Boolean = false,
    val horsepower: Int,
    val vehicleModel: String,
    val engineCapacity: Int,
    val doors: Int,
    val vehicleType: String,
    val color: String,
    val insuranceIncluded: Boolean
)

// Firestore Data Model for a Cart
data class Cart(
    val items: List<CartItem> = emptyList()
)

// Firestore Data Model for a Cart Item
data class CartItem(
    val carId: String = "",
    val quantity: Int = 0
)

// Firestore Data Model for a Booking
data class Booking(
    val id: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now()
)
