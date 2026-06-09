package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(db.financeDao())
    private val prefs = application.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
    
    private val _currentUserId = MutableStateFlow(prefs.getString("current_user", "") ?: "")
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isEmpty()) flowOf(emptyList()) else repository.getAllTransactions(userId)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val binTransactions = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isEmpty()) flowOf(emptyList()) else repository.getDeletedTransactions(userId)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
    fun refreshUser() {
        _currentUserId.value = prefs.getString("current_user", "") ?: ""
    }

    fun getCategoriesByType(type: String) = repository.getCategories(type).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome

    private val _monthlyExpense = MutableStateFlow(0.0)
    val monthlyExpense: StateFlow<Double> = _monthlyExpense

    init {
        viewModelScope.launch {
            // Setup default categories
            repository.getInitialCategories()
            repository.autoCleanupBin()
        }

        viewModelScope.launch {
            transactions.collectLatest { list ->
                val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                _totalBalance.value = income - expense
                // For simplicity, we just take all-time as monthly in MVP. 
                // In full version, filter by current month.
                _monthlyIncome.value = income
                _monthlyExpense.value = expense
            }
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, categoryName: String, desc: String, note: String, imageUri: String?, dateMillis: Long) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch
            repository.insertTransaction(
                Transaction(
                    userId = userId,
                    title = title,
                    amount = amount,
                    type = type,
                    categoryName = categoryName,
                    description = desc,
                    note = note,
                    imageUri = imageUri,
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun moveToBin(ids: List<Int>) {
        viewModelScope.launch {
            repository.moveToBin(ids)
        }
    }

    fun restoreFromBin(ids: List<Int>) {
        viewModelScope.launch {
            repository.restoreFromBin(ids)
        }
    }

    fun deletePermanently(ids: List<Int>) {
        viewModelScope.launch {
            repository.deletePermanently(ids)
        }
    }
}
