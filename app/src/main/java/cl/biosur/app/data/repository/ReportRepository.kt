package cl.biosur.app.data.repository

import cl.biosur.app.data.local.dao.EquipmentDao
import cl.biosur.app.data.local.dao.MaterialDao
import cl.biosur.app.data.local.dao.MediaDao
import cl.biosur.app.data.local.dao.PersonnelDao
import cl.biosur.app.data.local.dao.ReportDao
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.data.local.entity.ReportEntity
import cl.biosur.app.data.local.relation.ReportWithDetails
import cl.biosur.app.data.model.ReportStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de informes.
 * Se encarga de la creación/edición de un informe completo (cabecera + materiales
 * + equipos + personal + media + ubicación).
 */
class ReportRepository(
    private val reportDao: ReportDao,
    private val materialDao: MaterialDao,
    private val equipmentDao: EquipmentDao,
    private val personnelDao: PersonnelDao,
    private val mediaDao: MediaDao
) {

    fun observeAll(): Flow<List<ReportEntity>> = reportDao.observeAll()
    fun observeAllWithDetails(): Flow<List<ReportWithDetails>> = reportDao.observeAllWithDetails()
    fun observeBySupervisor(supervisorId: Long) = reportDao.observeBySupervisor(supervisorId)
    fun observeBySupervisorWithDetails(supervisorId: Long) =
        reportDao.observeBySupervisorWithDetails(supervisorId)
    fun observePendingSyncWithDetails() = reportDao.observePendingSyncWithDetails()
    fun observeCount() = reportDao.observeCount()
    fun observePendingSyncCount() = reportDao.observePendingSyncCount()

    suspend fun getWithDetails(id: Long) = reportDao.getWithDetails(id)

    /**
     * Crea un informe completo en una transacción lógica.
     * El [block] recibe el id del informe recién creado y debe devolver listas.
     */
    suspend fun saveFullReport(
        report: ReportEntity,
        materials: List<MaterialEntity>,
        equipment: List<EquipmentEntity>,
        personnel: List<PersonnelEntity>,
        media: List<MediaEntity>
    ): Long {
        val reportId = reportDao.insert(report.copy(updatedAt = System.currentTimeMillis()))
        // Si era un informe nuevo, asignamos reportId a cada detalle
        materialDao.deleteByReport(reportId)
        materialDao.insertAll(materials.map { it.copy(reportId = reportId) })

        equipmentDao.deleteByReport(reportId)
        equipmentDao.insertAll(equipment.map { it.copy(reportId = reportId) })

        personnelDao.deleteByReport(reportId)
        personnelDao.insertAll(personnel.map { it.copy(reportId = reportId) })

        mediaDao.deleteByReport(reportId)
        mediaDao.insertAll(media.map { it.copy(reportId = reportId) })

        return reportId
    }

    suspend fun updateStatus(reportId: Long, status: ReportStatus) {
        val report = reportDao.findById(reportId) ?: return
        reportDao.update(
            report.copy(
                status = status.value,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markSynced(reportId: Long) = reportDao.markSynced(reportId)

    suspend fun delete(reportId: Long) = reportDao.deleteById(reportId)
}
