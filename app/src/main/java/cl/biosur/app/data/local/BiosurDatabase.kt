package cl.biosur.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cl.biosur.app.data.local.dao.EmployeeDao
import cl.biosur.app.data.local.dao.EquipmentDao
import cl.biosur.app.data.local.dao.MaterialDao
import cl.biosur.app.data.local.dao.MediaDao
import cl.biosur.app.data.local.dao.PersonnelDao
import cl.biosur.app.data.local.dao.ReportDao
import cl.biosur.app.data.local.dao.UserDao
import cl.biosur.app.data.local.entity.EmployeeEntity
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.data.local.entity.ReportEntity
import cl.biosur.app.data.local.entity.UserEntity

/**
 * Base de datos local BIOSUR (Room).
 *
 * Versión 1. Si más adelante agregan columnas, suban [version] y agreguen
 * un Migration en [MIGRATIONS] o usen fallbackToDestructiveMigration en desarrollo.
 */
@Database(
    entities = [
        UserEntity::class,
        EmployeeEntity::class,
        ReportEntity::class,
        MaterialEntity::class,
        EquipmentEntity::class,
        PersonnelEntity::class,
        MediaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BiosurDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun reportDao(): ReportDao
    abstract fun materialDao(): MaterialDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun personnelDao(): PersonnelDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: BiosurDatabase? = null

        fun get(context: Context): BiosurDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BiosurDatabase::class.java,
                    "biosur.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
