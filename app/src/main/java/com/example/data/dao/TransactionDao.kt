package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Transaction
    @Query("SELECT * FROM transactions WHERE client_id = :clientId ORDER BY date_credit DESC, created_at DESC")
    fun getTransactionsWithItemsForClient(clientId: String): Flow<List<TransactionWithItems>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date_credit DESC, created_at DESC")
    fun getAllTransactionsWithItems(): Flow<List<TransactionWithItems>>

    @Query("SELECT * FROM transactions ORDER BY date_credit ASC, created_at ASC")
    suspend fun getAllTransactionsChronological(): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY date_credit DESC, created_at DESC LIMIT 1")
    suspend fun getLastTransaction(): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE client_id = :clientId ORDER BY date_credit ASC, created_at ASC")
    suspend fun getTransactionsForClientDirect(clientId: String): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionWithItemsById(transactionId: String): TransactionWithItems?

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalTransactionsCount(): Flow<Int>
}
