package cl.biosur.app

import cl.biosur.app.data.local.BiosurDatabase
import cl.biosur.app.data.repository.AuthRepository
import cl.biosur.app.data.repository.CompanyRepository
import cl.biosur.app.data.repository.EmployeeRepository
import cl.biosur.app.data.repository.ReportRepository
import cl.biosur.app.util.SessionManager

/**
 * Mini "Service Locator" manual.
 *
 * Alternativa: usar Hilt/Koin si quieren migrar a DI formal.
 * Por ahora, este objeto expone los repositorios compartidos a los ViewModels.
 */
object ServiceLocator {

    private val db by lazy { BiosurDatabase.get(BiosurApp.get()) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(db.userDao(), SessionManager)
    }

    val employeeRepository: EmployeeRepository by lazy {
        EmployeeRepository(db.employeeDao())
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(
            db.reportDao(),
            db.materialDao(),
            db.equipmentDao(),
            db.personnelDao(),
            db.mediaDao()
        )
    }

    val companyRepository: CompanyRepository by lazy {
        CompanyRepository(db.reportDao(), db.userDao(), db.employeeDao())
    }
}
