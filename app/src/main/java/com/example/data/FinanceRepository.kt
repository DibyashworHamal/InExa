package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FinanceRepository(private val financeDao: FinanceDao) {
    fun getAllTransactions(userId: String): Flow<List<Transaction>> = financeDao.getAllTransactions(userId)
    fun getDeletedTransactions(userId: String): Flow<List<Transaction>> = financeDao.getDeletedTransactions(userId)

    fun getCategories(type: String): Flow<List<Category>> = financeDao.getCategoriesByType(type)

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        financeDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun moveToBin(ids: List<Int>, deleteTime: Long = System.currentTimeMillis()) {
        financeDao.moveToBin(ids, deleteTime)
    }

    suspend fun restoreFromBin(ids: List<Int>) {
        financeDao.restoreFromBin(ids)
    }

    suspend fun deletePermanently(ids: List<Int>) {
        financeDao.deletePermanently(ids)
    }

    suspend fun autoCleanupBin() {
        val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days
        financeDao.deleteOldBinItems(threshold)
    }

    suspend fun insertCategory(category: Category) {
        financeDao.insertCategory(category)
    }
    
    suspend fun getInitialCategories() {
        val incomeCategories = listOf("Salary", "Freelancing", "Business", "Gift", "Investment", "Other")
        val expenseCategories = listOf("Food", "Transport", "Shopping", "Education", "Health", "Entertainment", "Bills", "Rent", "Travel", "Other")
        
        if (financeDao.getCategoryCount() == 0) {
            incomeCategories.forEach { insertCategory(Category(name = it, type = "INCOME")) }
            expenseCategories.forEach { insertCategory(Category(name = it, type = "EXPENSE")) }
        }
    }

    fun getAllNotes(userId: String): Flow<List<Note>> = financeDao.getAllNotes(userId)

    suspend fun insertNote(note: Note) {
        financeDao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        financeDao.deleteNote(note)
    }
}
