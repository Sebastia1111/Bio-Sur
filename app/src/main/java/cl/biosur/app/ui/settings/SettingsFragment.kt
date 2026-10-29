package cl.biosur.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cl.biosur.app.MainActivity
import cl.biosur.app.ServiceLocator
import cl.biosur.app.databinding.FragmentSettingsBinding
import cl.biosur.app.util.BackupSyncManager
import cl.biosur.app.util.SessionManager
import cl.biosur.app.util.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStreamReader

/**
 * Pantalla de Configuración y Ajustes.
 * Permite cambiar de tema (Claro/Oscuro/Sistema), gestionar perfil,
 * exportar/importar respaldos JSON y cerrar sesión.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            importBackupFromUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        setupThemeSelector()
        setupSyncActions()
        setupLogout()
        updateDatabaseStatus()
    }

    private fun setupUserProfile() {
        val name = SessionManager.currentFullName() ?: "Usuario BIOSUR"
        val email = SessionManager.currentEmail() ?: "sin_correo@biosur.cl"
        val role = SessionManager.currentRole() ?: "Supervisor"

        binding.tvUserName.text = name
        binding.tvUserEmail.text = email
        binding.chipUserRole.text = role.replaceFirstChar { it.uppercase() }
    }

    private fun setupThemeSelector() {
        val currentTheme = ThemeManager.getSavedTheme(requireContext())
        when (currentTheme) {
            ThemeManager.THEME_LIGHT -> binding.rbThemeLight.isChecked = true
            ThemeManager.THEME_DARK -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val newThemeMode = when (checkedId) {
                binding.rbThemeLight.id -> ThemeManager.THEME_LIGHT
                binding.rbThemeDark.id -> ThemeManager.THEME_DARK
                else -> ThemeManager.THEME_SYSTEM
            }
            ThemeManager.setSavedTheme(requireContext(), newThemeMode)
        }
    }

    private fun setupSyncActions() {
        binding.btnExportJson.setOnClickListener {
            lifecycleScope.launch {
                val uri = BackupSyncManager.createBackupFile(requireContext())
                if (uri != null) {
                    val intent = BackupSyncManager.createShareIntent(requireContext(), uri)
                    startActivity(Intent.createChooser(intent, "Compartir copia de seguridad"))
                } else {
                    Snackbar.make(binding.root, "Error al generar el archivo de respaldo", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnImportJson.setOnClickListener {
            importJsonLauncher.launch("*/*")
        }
    }

    private fun importBackupFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val jsonString = inputStream?.use { InputStreamReader(it).readText() }.orEmpty()

                if (jsonString.isBlank()) {
                    Toast.makeText(requireContext(), "Archivo inválido o vacío", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val count = BackupSyncManager.importBackupFromJson(jsonString)
                if (count > 0) {
                    Snackbar.make(
                        binding.root,
                        "¡Se importaron exitosamente $count informe(s)!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    updateDatabaseStatus()
                } else {
                    Toast.makeText(requireContext(), "No se pudieron importar informes del archivo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al leer el archivo JSON", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión") { _, _ ->
                    (activity as? MainActivity)?.logout()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun updateDatabaseStatus() {
        lifecycleScope.launch {
            try {
                val reports = ServiceLocator.reportRepository.observeCount().first()
                binding.tvDbStatus.text = "Informes guardados localmente: $reports"
            } catch (e: Exception) {
                binding.tvDbStatus.text = "Informes guardados localmente"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
