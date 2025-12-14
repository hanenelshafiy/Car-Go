package com.example.car_go

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Car::class], version = 1, exportSchema = false)
abstract class CarGoDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var INSTANCE: CarGoDatabase? = null

        fun getDatabase(context: Context): CarGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CarGoDatabase::class.java,
                    "car_go_database"
                )
                .addCallback(CarGoDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private class CarGoDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Pre-populate the database with mock data
        val database = CarGoDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            database.carDao().insertAll(mockCarData)
        }
    }
}

// Mock data for pre-population
val mockCarData = listOf(
    Car("C001", "Tesla", "Model Y", 2024, 49990.00, "High-performance electric SUV with long range and autopilot.", "tesla_model_y", true, 455, "SUV", 0, 5, "SUV", "White", true),
    Car("C002", "Ford", "Mustang", 1969, 55000.00, "Classic American muscle car, restored and powerful V8 engine.", "ford_mustang", false, 335, "Coupe", 5000, 2, "Muscle Car", "Red", false),
    Car("C003", "Honda", "Civic", 2023, 24500.00, "Reliable and fuel-efficient sedan, perfect for city driving.", "honda_civic", false, 158, "Sedan", 2000, 4, "Sedan", "Blue", true),
    Car("C004", "Porsche", "911 GT3", 2022, 182900.00, "Track-ready performance vehicle with iconic design and racing chassis.", "porsche_911_gt3", false, 502, "Coupe", 4000, 2, "Sports Car", "Black", false),
    Car("C005", "Rivian", "R1T", 2023, 79000.00, "Electric adventure vehicle with quad-motor drive and impressive off-road capabilities.", "rivian_r1t", true, 835, "Truck", 0, 4, "Truck", "Green", true),
    Car("C006", "Lucid", "Air", 2024, 82400.00, "Luxury electric sedan with industry-leading range and a spacious, futuristic interior.", "lucid_air", true, 1200, "Sedan", 0, 4, "Sedan", "Silver", true),
    Car("C007", "BMW", "i4", 2024, 52200.00, "The first-ever all-electric Gran Coupé, offering a blend of sportiness and comfort.", "bmw_i4", true, 335, "Sedan", 0, 4, "Sedan", "Gray", true),
    Car("C008", "Toyota", "Camry", 2024, 26420.00, "A best-selling sedan known for its reliability, comfort, and safety features.", "toyota_camry", false, 203, "Sedan", 2500, 4, "Sedan", "White", true),
    Car("C009", "Jeep", "Wrangler", 2024, 31895.00, "The iconic off-road SUV with legendary capability and open-air freedom.", "jeep_wrangler", false, 285, "SUV", 3600, 4, "SUV", "Black", false),
    Car("C010", "Chevrolet", "Corvette", 2024, 68300.00, "The legendary American sports car, with a mid-engine design for optimal performance.", "chevrolet_corvette", false, 490, "Coupe", 6200, 2, "Sports Car", "Red", false),
    Car("C011", "Hyundai", "Sonata", 2024, 27500.00, "A stylish and modern sedan with a spacious interior and a host of advanced tech features.", "hyundai_sonata", false, 191, "Sedan", 2500, 4, "Sedan", "Blue", true),
    Car("C012", "GMC", "Sierra", 2024, 37200.00, "A premium full-size pickup truck that offers a refined interior and powerful engine options.", "gmc_sierra", false, 355, "Truck", 5300, 4, "Truck", "White", false)
)
