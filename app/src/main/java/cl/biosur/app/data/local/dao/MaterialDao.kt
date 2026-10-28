package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.biosur.app.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialEntity>): List<Long>

    @Delete
    suspend fun delete(material: MaterialEntity)

    @Query("SELECT * FROM materials WHERE reportId = :reportId")
    fun observeByReport(reportId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<MaterialEntity>

    @Query("DELETE FROM materials WHERE reportId = :reportId")
    suspend fun deleteByReport(reportId: Long)
}
