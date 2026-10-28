package cl.biosur.app.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Converters Room (por si en el futuro se usan tipos complejos).
 * De momento sólo timestamps como Long, pero lo dejamos instalado.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
