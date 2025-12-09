// package com.example.car_go.data
package com.example.car_go.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Core Data Model for a Car (not Room — just in-memory)
data class Car(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val isElectric: Boolean = false
)

// Room Entity for Shopping Cart
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val carId: String,
    val quantity: Int
)