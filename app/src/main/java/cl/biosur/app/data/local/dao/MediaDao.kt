package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.biosur.app.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaEntity>): List<Long>

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("SELECT * FROM media WHERE reportId = :reportId ORDER BY capturedAt DESC")
    fun observeByReport(reportId: Long): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<MediaEntity>

    @Query("DELETE FROM media WHERE reportId = :reportId")
    suspend fun deleteByReport(reportId: Long)
}
