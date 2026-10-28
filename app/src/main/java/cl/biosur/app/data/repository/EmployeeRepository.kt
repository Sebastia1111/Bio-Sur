package cl.biosur.app.data.repository

import cl.biosur.app.data.local.dao.EmployeeDao
import cl.biosur.app.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de empleados (CRUD).
 *
 * Responsabilidad del COMPAÑERO 1 (junto con Auth).
 */
class EmployeeRepository(private val employeeDao: EmployeeDao) {

    fun observeAll(): Flow<List<EmployeeEntity>> = employeeDao.observeAll()
    fun observeActive(): Flow<List<EmployeeEntity>> = employeeDao.observeActive()
    fun observeActiveCount(): Flow<Int> = employeeDao.observeActiveCount()

    suspend fun findById(id: Long): EmployeeEntity? = employeeDao.findById(id)

    suspend fun create(employee: EmployeeEntity): Long = employeeDao.insert(employee)

    suspend fun update(employee: EmployeeEntity) = employeeDao.update(employee)

    suspend fun delete(employee: EmployeeEntity) = employeeDao.delete(employee)
}
