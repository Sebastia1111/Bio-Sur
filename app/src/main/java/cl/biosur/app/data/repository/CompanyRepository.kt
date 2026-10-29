package cl.biosur.app.data.repository

import cl.biosur.app.data.local.dao.EmployeeDao
import cl.biosur.app.data.local.dao.ReportDao
import cl.biosur.app.data.local.dao.UserDao
import cl.biosur.app.data.local.relation.ReportWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repositorio del panel empresa (rol COMPANY).
 *
 */
class CompanyRepository(
    private val reportDao: ReportDao,
    private val userDao: UserDao,
    private val employeeDao: EmployeeDao
) {

    fun observeAllReports(): Flow<List<ReportWithDetails>> = reportDao.observeAllWithDetails()

    fun observePendingSync(): Flow<List<ReportWithDetails>> = reportDao.observePendingSyncWithDetails()

    fun observeDashboardStats(): Flow<DashboardStats> =
        combine(
            reportDao.observeCount(),
            userDao.observeSupervisorCount(),
            employeeDao.observeActiveCount(),
            reportDao.observePendingSyncCount()
        ) { totalReports, totalSupervisors, activeEmployees, pendingSync ->
            DashboardStats(
                totalReports = totalReports,
                totalSupervisors = totalSupervisors,
                activeEmployees = activeEmployees,
                pendingSync = pendingSync
            )
        }

    data class DashboardStats(
        val totalReports: Int,
        val totalSupervisors: Int,
        val activeEmployees: Int,
        val pendingSync: Int
    )
}
