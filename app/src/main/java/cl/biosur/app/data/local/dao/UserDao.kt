package cl.biosur.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.biosur.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'SUPERVISOR' ORDER BY fullName")
    fun observeSupervisors(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'SUPERVISOR'")
    fun observeSupervisorCount(): Flow<Int>

    @Query("UPDATE users SET active = :active WHERE id = :userId")
    suspend fun setActive(userId: Long, active: Boolean)
}
