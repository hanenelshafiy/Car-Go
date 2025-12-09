// package com.example.car_go
package com.example.car_go

import com.example.car_go.data.CartItemEntity
import com.example.car_go.data.Car
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Singleton repository. MainActivity must set database before DB operations.
 */
object CarRepository {
    // Will be set in MainActivity (before setContent)
    lateinit var database: com.example.car_go.data.CarGoDatabase
    private val dao get() = database.cartItemDao()

    // Mock cars (you can later seed DB or replace)
    private val mockCarData = listOf(
        Car("C001", "Tesla", "Model Y", 2024, 49990.00, "High-performance electric SUV with long range and autopilot.", "https://placehold.co/300x200/1C3C56/FFFFFF?text=Model+Y", true),
        Car("C002", "Ford", "Mustang", 1969, 55000.00, "Classic American muscle car, restored and powerful V8 engine.", "https://placehold.co/300x200/B22222/FFFFFF?text=Mustang"),
        Car("C003", "Honda", "Civic", 2023, 24500.00, "Reliable and fuel-efficient sedan, perfect for city driving.", "https://placehold.co/300x200/3A5FCD/FFFFFF?text=Civic"),
        Car("C004", "Porsche", "911 GT3", 2022, 182900.00, "Track-ready performance vehicle with iconic design and racing chassis.", "https://placehold.co/300x200/121212/FFFFFF?text=911+GT3"),
    )

    private val _cars = MutableStateFlow<List<Car>>(mockCarData)
    val cars = _cars.asStateFlow()

    fun getCartItems(): Flow<List<CartItemEntity>> = dao.getAllItems()

    suspend fun upsertCartItem(carId: String) {
        val existing = dao.getItemByCarId(carId)
        if (existing != null) {
            // insert with same primary key id -> replace (onConflict = REPLACE)
            dao.insert(existing.copy(quantity = existing.quantity + 1))
        } else {
            dao.insert(CartItemEntity(carId = carId, quantity = 1))
        }
    }

    suspend fun deleteCartItem(item: CartItemEntity) {
        dao.delete(item)
    }

    fun getCarById(carId: String): Car? = _cars.value.find { it.id == carId }
}

class CarViewModel(private val repository: CarRepository = CarRepository) : ViewModel() {

    val cars: StateFlow<List<Car>> = repository.cars

    // cartItems flow turned into a state flow for UI consumption
    val cartItems: StateFlow<List<CartItemEntity>> =
        repository.getCartItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCart(carId: String) {
        viewModelScope.launch { repository.upsertCartItem(carId) }
    }

    fun removeFromCart(item: CartItemEntity) {
        viewModelScope.launch { repository.deleteCartItem(item) }
    }

    // total price
    val cartTotal: StateFlow<Double> = combine(cars, cartItems) { carList, cartItemList ->
        cartItemList.sumOf { cartItem ->
            carList.find { it.id == cartItem.carId }?.price?.times(cartItem.quantity) ?: 0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun getCarById(carId: String): StateFlow<Car?> {
        return cars.map { list -> list.find { it.id == carId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}