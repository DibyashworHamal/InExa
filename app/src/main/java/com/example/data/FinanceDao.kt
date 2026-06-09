package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY dateMillis DESC")
    fun getAllTransactions(userId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 1 ORDER BY dateMillis DESC")
    fun getDeletedTransactions(userId: String): Flow<List<Transaction>>

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET isDeleted = 1, deletedDateMillis = :deleteTime WHERE id IN (:ids)")
    suspend fun moveToBin(ids: List<Int>, deleteTime: Long)

    @Query("UPDATE transactions SET isDeleted = 0, deletedDateMillis = null WHERE id IN (:ids)")
    suspend fun restoreFromBin(ids: List<Int>)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deletePermanently(ids: List<Int>)

    @Query("DELETE FROM transactions WHERE isDeleted = 1 AND deletedDateMillis < :thresholdMillis")
    suspend fun deleteOldBinItems(thresholdMillis: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY dateMillis DESC")
    fun getAllNotes(userId: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}
