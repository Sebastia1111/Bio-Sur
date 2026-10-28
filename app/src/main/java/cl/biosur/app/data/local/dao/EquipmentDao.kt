package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.biosur.app.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: EquipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EquipmentEntity>): List<Long>

    @Delete
    suspend fun delete(equipment: EquipmentEntity)

    @Query("SELECT * FROM equipment WHERE reportId = :reportId")
    fun observeByReport(reportId: Long): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<EquipmentEntity>

    @Query("DELETE FROM equipment WHERE reportId = :reportId")
    suspend fun deleteByReport(reportId: Long)
}
