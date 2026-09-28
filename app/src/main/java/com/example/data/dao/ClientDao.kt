package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Query("SELECT * FROM clients WHERE statut = 'ACTIF' ORDER BY nom ASC")
    fun getAllActiveClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients ORDER BY nom ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: String): Client?

    @Query("SELECT * FROM clients WHERE cin = :cin LIMIT 1")
    suspend fun getClientByCin(cin: String): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)

    @Query("UPDATE clients SET statut = 'ARCHIVE', updated_at = :updatedAt WHERE id = :id")
    suspend fun archiveClient(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM clients WHERE statut = 'ACTIF'")
    fun getActiveClientCount(): Flow<Int>
}
