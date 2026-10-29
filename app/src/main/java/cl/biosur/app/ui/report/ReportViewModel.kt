package cl.biosur.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.biosur.app.ServiceLocator
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.data.local.entity.ReportEntity
import cl.biosur.app.data.local.relation.ReportWithDetails
import cl.biosur.app.data.model.ReportStatus
import cl.biosur.app.data.repository.ReportRepository
import cl.biosur.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel compartido entre [ReportListFragment] y [ReportFormFragment].
 */
class ReportViewModel : ViewModel() {

    private val repo: ReportRepository = ServiceLocator.reportRepository

    val reports: StateFlow<List<ReportWithDetails>> =
        repo.observeAllWithDetails().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> =
        repo.observePendingSyncCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ===== Estado del formulario =====
    private val _draftMaterials = MutableStateFlow<List<MaterialEntity>>(emptyList())
    val draftMaterials: StateFlow<List<MaterialEntity>> = _draftMaterials.asStateFlow()

    private val _draftEquipment = MutableStateFlow<List<EquipmentEntity>>(emptyList())
    val draftEquipment: StateFlow<List<EquipmentEntity>> = _draftEquipment.asStateFlow()

    private val _draftPersonnel = MutableStateFlow<List<PersonnelEntity>>(emptyList())
    val draftPersonnel: StateFlow<List<PersonnelEntity>> = _draftPersonnel.asStateFlow()

    private val _draftMedia = MutableStateFlow<List<MediaEntity>>(emptyList())
    val draftMedia: StateFlow<List<MediaEntity>> = _draftMedia.asStateFlow()

    private val _draftLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val draftLocation: StateFlow<Pair<Double, Double>?> = _draftLocation.asStateFlow()

    private val _draftAddress = MutableStateFlow<String?>(null)
    val draftAddress: StateFlow<String?> = _draftAddress.asStateFlow()

    var reportDate: Long = System.currentTimeMillis()
    var startTime: Long = System.currentTimeMillis()
    var endTime: Long = System.currentTimeMillis() + 3600000L
    var comments: String = ""

    data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

    fun validateForm(): ValidationResult {
        if (SessionManager.currentUserId() == null) {
            return ValidationResult(false, "Debes haber iniciado sesión para enviar un informe")
        }
        if (endTime <= startTime) {
            return ValidationResult(false, "La hora de término debe ser posterior a la hora de inicio")
        }
        val hasMaterials = _draftMaterials.value.isNotEmpty()
        val hasEquipment = _draftEquipment.value.isNotEmpty()
        val hasPersonnel = _draftPersonnel.value.isNotEmpty()
        val hasMedia = _draftMedia.value.isNotEmpty()
        val hasComments = comments.trim().isNotEmpty()
        val hasLocation = _draftLocation.value != null

        if (!hasMaterials && !hasEquipment && !hasPersonnel && !hasMedia && !hasComments && !hasLocation) {
            return ValidationResult(
                false,
                "Completa al menos una sección del informe (comentarios, evidencias, materiales, equipos o personal)"
            )
        }
        return ValidationResult(true)
    }

    // ===== Operaciones de borrador =====
    fun addMaterial(m: MaterialEntity) { _draftMaterials.value = _draftMaterials.value + m }
    fun removeMaterial(m: MaterialEntity) { _draftMaterials.value = _draftMaterials.value - m }

    fun addEquipment(e: EquipmentEntity) { _draftEquipment.value = _draftEquipment.value + e }
    fun removeEquipment(e: EquipmentEntity) { _draftEquipment.value = _draftEquipment.value - e }

    fun addPersonnel(p: PersonnelEntity) { _draftPersonnel.value = _draftPersonnel.value + p }
    fun removePersonnel(p: PersonnelEntity) { _draftPersonnel.value = _draftPersonnel.value - p }

    fun addMedia(m: MediaEntity) { _draftMedia.value = _draftMedia.value + m }
    fun removeMedia(m: MediaEntity) { _draftMedia.value = _draftMedia.value - m }

    fun setLocation(lat: Double, lon: Double, address: String? = null) {
        _draftLocation.value = lat to lon
        _draftAddress.value = address
    }

    // ===== Guardar / actualizar =====
    fun saveReport(
        onSaved: (Long) -> Unit,
        address: String? = null
    ) {
        viewModelScope.launch {
            val supervisorId = SessionManager.currentUserId() ?: return@launch
            val finalAddress = address ?: _draftAddress.value
            val report = ReportEntity(
                supervisorId = supervisorId,
                date = reportDate,
                startTime = startTime,
                endTime = endTime,
                comments = comments,
                latitude = _draftLocation.value?.first,
                longitude = _draftLocation.value?.second,
                address = finalAddress,
                status = ReportStatus.READY.value,
                synced = false
            )
            val id = repo.saveFullReport(
                report = report,
                materials = _draftMaterials.value,
                equipment = _draftEquipment.value,
                personnel = _draftPersonnel.value,
                media = _draftMedia.value
            )
            resetDraft()
            onSaved(id)
        }
    }

    fun markExported(reportId: Long) {
        viewModelScope.launch {
            repo.markSynced(reportId)
        }
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch { repo.delete(reportId) }
    }

    fun resetDraft() {
        _draftMaterials.value = emptyList()
        _draftEquipment.value = emptyList()
        _draftPersonnel.value = emptyList()
        _draftMedia.value = emptyList()
        _draftLocation.value = null
        _draftAddress.value = null
        reportDate = System.currentTimeMillis()
        startTime = System.currentTimeMillis()
        endTime = System.currentTimeMillis() + 3600000L
        comments = ""
    }
}
