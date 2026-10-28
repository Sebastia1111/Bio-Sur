package cl.biosur.app.ui.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.biosur.app.ServiceLocator
import cl.biosur.app.data.repository.CompanyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del panel empresa.
 * Responsabilidad: Compañero 3.
 */
class CompanyDashboardViewModel : ViewModel() {

    private val repo: CompanyRepository = ServiceLocator.companyRepository

    val stats: StateFlow<CompanyRepository.DashboardStats> =
        repo.observeDashboardStats().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            CompanyRepository.DashboardStats(0, 0, 0, 0)
        )

    val recentReports = repo.observeAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSupervisor(
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                ServiceLocator.authRepository.createSupervisorAccount(email, password, fullName)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear cuenta de supervisor")
            }
        }
    }
}
