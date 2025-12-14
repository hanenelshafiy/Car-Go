package com.example.car_go

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Run tests against a supported API level
class CarDaoTest {

    private lateinit var database: CarGoDatabase
    private lateinit var carDao: CarDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CarGoDatabase::class.java)
            .allowMainThreadQueries() // Only for testing
            .build()
        carDao = database.carDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertAll and getAllCars`() = runBlocking {
        // Given
        val cars = listOf(
            Car("C001", "Tesla", "Model Y", 2024, 49990.00, "", "", true, 0, "", 0, 0, "", "", false),
            Car("C002", "Ford", "Mustang", 1969, 55000.00, "", "", false, 0, "", 0, 0, "", "", false)
        )

        // When
        carDao.insertAll(cars)

        // Then
        val allCars = carDao.getAllCars().first()
        assertThat(allCars).isEqualTo(cars)
        assertThat(allCars.size).isEqualTo(2)
    }

    @Test
    fun `getCarById returns correct car`() = runBlocking {
        // Given
        val car = Car("C001", "Tesla", "Model Y", 2024, 49990.00, "", "", true, 0, "", 0, 0, "", "", false)
        carDao.insertAll(listOf(car))

        // When
        val result = carDao.getCarById("C001").first()

        // Then
        assertThat(result).isEqualTo(car)
    }
}