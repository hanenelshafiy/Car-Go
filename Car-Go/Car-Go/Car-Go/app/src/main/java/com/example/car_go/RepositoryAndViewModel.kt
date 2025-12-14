package com.example.car_go

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- DATA LAYER ---

class CarRepository(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth, private val carDao: CarDao) {

    val cars: Flow<List<Car>> = carDao.getAllCars()

    fun getCarById(carId: String): Flow<Car?> = carDao.getCarById(carId)

    fun getCartItems(): Flow<List<CartItem>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run { close(IllegalStateException("User not logged in")); return@callbackFlow }
        val cartRef = firestore.collection("carts").document(userId)
        val subscription = cartRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject<Cart>()?.let { trySend(it.items) }
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun upsertCartItem(carId: String) {
        val userId = auth.currentUser?.uid ?: return
        val cartRef = firestore.collection("carts").document(userId)
        firestore.runTransaction { transaction ->
            val cart = transaction.get(cartRef).toObject<Cart>() ?: Cart()
            val existingItem = cart.items.find { it.carId == carId }
            val newItems = if (existingItem != null) {
                cart.items.map { if (it.carId == carId) it.copy(quantity = it.quantity + 1) else it }
            } else {
                cart.items + CartItem(carId, 1)
            }
            transaction.set(cartRef, Cart(newItems))
        }.await()
    }

    suspend fun deleteCartItem(carId: String) {
        val userId = auth.currentUser?.uid ?: return
        val cartRef = firestore.collection("carts").document(userId)
        firestore.runTransaction { transaction ->
            val cart = transaction.get(cartRef).toObject<Cart>() ?: return@runTransaction
            val newItems = cart.items.filter { it.carId != carId }
            transaction.set(cartRef, Cart(newItems))
        }.await()
    }

    suspend fun createBooking(cart: Cart, totalPrice: Double): String {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val bookingsRef = firestore.collection("users").document(userId).collection("bookings")
        val booking = Booking(items = cart.items, totalPrice = totalPrice)
        val documentRef = bookingsRef.add(booking).await()
        firestore.collection("carts").document(userId).set(Cart(emptyList())).await()
        return documentRef.id
    }

    fun getBookings(): Flow<List<Booking>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run { close(IllegalStateException("User not logged in")); return@callbackFlow }
        val bookingsRef = firestore.collection("users").document(userId).collection("bookings")
        val subscription = bookingsRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val bookings = snapshot.documents.mapNotNull {
                    it.toObject<Booking>()?.copy(id = it.id)
                }
                trySend(bookings)
            }
        }
        awaitClose { subscription.remove() }
    }
}

// --- VIEWMODEL ---

class CarViewModel(private val repository: CarRepository) : ViewModel() {

    val cars: StateFlow<List<Car>> = repository.cars.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addToCart(carId: String) { viewModelScope.launch { repository.upsertCartItem(carId) } }
    fun removeFromCart(carId: String) { viewModelScope.launch { repository.deleteCartItem(carId) } }

    val cartTotal: StateFlow<Double> = combine(cars, cartItems) { carList, cartItemList ->
        cartItemList.sumOf { cartItem -> carList.find { it.id == cartItem.carId }?.price?.times(cartItem.quantity) ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun getBookings(): Flow<List<Booking>> = repository.getBookings()

    fun createBooking(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val bookingId = repository.createBooking(Cart(cartItems.value), cartTotal.value)
            onComplete(bookingId)
        }
    }

    fun getCarById(carId: String): StateFlow<Car?> = repository.getCarById(carId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CarGoApplication
                return CarViewModel(application.repository) as T
            }
        }
    }
}

// --- APPLICATION CLASS ---

class CarGoApplication : Application() {
    private val database by lazy { CarGoDatabase.getDatabase(this) }
    val repository: CarRepository by lazy {
        CarRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance(), database.carDao())
    }
}
