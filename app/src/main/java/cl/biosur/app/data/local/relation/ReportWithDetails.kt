package cl.biosur.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.data.local.entity.ReportEntity

/**
 * Informe completo con todos sus detalles relacionados.
 * Se obtiene con @Transaction en [cl.biosur.app.data.local.dao.ReportDao].
 */
data class ReportWithDetails(
    @Embedded val report: ReportEntity,

    @Relation(parentColumn = "id", entityColumn = "reportId")
    val materials: List<MaterialEntity>,

    @Relation(parentColumn = "id", entityColumn = "reportId")
    val equipment: List<EquipmentEntity>,

    @Relation(parentColumn = "id", entityColumn = "reportId")
    val personnel: List<PersonnelEntity>,

    @Relation(parentColumn = "id", entityColumn = "reportId")
    val media: List<MediaEntity>
)
