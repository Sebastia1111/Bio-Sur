package cl.biosur.app.ui.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.biosur.app.ServiceLocator
import cl.biosur.app.data.local.entity.EmployeeEntity
import cl.biosur.app.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmployeeViewModel : ViewModel() {

    private val repo: EmployeeRepository = ServiceLocator.employeeRepository

    val employees: StateFlow<List<EmployeeEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getEmployee(id: Long): EmployeeEntity? = repo.findById(id)

    fun createOrUpdate(employee: EmployeeEntity) {
        viewModelScope.launch {
            if (employee.id == 0L) repo.create(employee) else repo.update(employee)
        }
    }

    fun delete(employee: EmployeeEntity) {
        viewModelScope.launch { repo.delete(employee) }
    }
}
