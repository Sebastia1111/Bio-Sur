package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.biosur.app.data.local.entity.PersonnelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonnelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personnel: PersonnelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PersonnelEntity>): List<Long>

    @Delete
    suspend fun delete(personnel: PersonnelEntity)

    @Query("SELECT * FROM personnel WHERE reportId = :reportId")
    fun observeByReport(reportId: Long): Flow<List<PersonnelEntity>>

    @Query("SELECT * FROM personnel WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<PersonnelEntity>

    @Query("DELETE FROM personnel WHERE reportId = :reportId")
    suspend fun deleteByReport(reportId: Long)
}
