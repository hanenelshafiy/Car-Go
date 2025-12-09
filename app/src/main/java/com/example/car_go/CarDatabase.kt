// package com.example.car_go.data
package com.example.car_go.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = false)
abstract class CarGoDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDao
}