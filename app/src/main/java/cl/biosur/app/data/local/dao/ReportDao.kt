package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.biosur.app.data.local.entity.ReportEntity
import cl.biosur.app.data.local.relation.ReportWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity): Long

    @Update
    suspend fun update(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteById(reportId: Long)

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ReportEntity?

    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE supervisorId = :supervisorId ORDER BY date DESC")
    fun observeBySupervisor(supervisorId: Long): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reports WHERE synced = 0")
    fun observePendingSyncCount(): Flow<Int>

    @Query("UPDATE reports SET synced = 1, updatedAt = :now WHERE id = :reportId")
    suspend fun markSynced(reportId: Long, now: Long = System.currentTimeMillis())

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getWithDetails(id: Long): ReportWithDetails?

    @Transaction
    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun observeAllWithDetails(): Flow<List<ReportWithDetails>>

    @Transaction
    @Query("SELECT * FROM reports WHERE supervisorId = :supervisorId ORDER BY date DESC")
    fun observeBySupervisorWithDetails(supervisorId: Long): Flow<List<ReportWithDetails>>

    @Transaction
    @Query("SELECT * FROM reports WHERE synced = 0 ORDER BY date DESC")
    fun observePendingSyncWithDetails(): Flow<List<ReportWithDetails>>
}
