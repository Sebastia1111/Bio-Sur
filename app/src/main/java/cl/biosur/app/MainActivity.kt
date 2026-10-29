package cl.biosur.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cl.biosur.app.databinding.ActivityMainBinding
import cl.biosur.app.util.SessionManager
import cl.biosur.app.util.ThemeManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        setTheme(R.style.Theme_BIOSUR)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val host = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        navController = host.navController

        binding.bottomNav.setupWithNavController(navController)

        // Control dinámico de visibilidad del menú inferior
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.reportFormFragment,
                R.id.reportDetailFragment,
                R.id.employeeFormFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    // Si intenta navegar a pantallas internas sin sesión, redirigir al login
                    if (!SessionManager.isLoggedIn()) {
                        binding.bottomNav.visibility = View.GONE
                        navController.navigate(R.id.loginFragment)
                    } else {
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /** Cierra sesión y vuelve al login. */
    fun logout() {
        lifecycleScope.launch {
            ServiceLocator.authRepository.logout()
            navController.navigate(R.id.loginFragment)
        }
    }
}
