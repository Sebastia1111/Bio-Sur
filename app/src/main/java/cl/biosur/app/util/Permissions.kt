package cl.biosur.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Helper de permisos en tiempo de ejecución (Android 6+).
 * Pensado para usarse desde un Fragment.
 *
 * Uso típico:
 *   private lateinit var perms: Permissions
 *   override fun onViewCreated(...) {
 *       perms = Permissions(this) { granted -> ... }
 *   }
 *   perms.requestCameraAndLocation()
 */
class Permissions(
    private val fragment: Fragment,
    private val onResult: (Map<String, Boolean>) -> Unit
) {
    private val launcher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onResult(it)
        }

    fun isCameraGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun isFineLocationGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun isCoarseLocationGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun isNotificationGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun isMediaGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun requestCameraAndLocation() {
        val list = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }
        launcher.launch(list.toTypedArray())
    }

    fun requestMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        }
    }
}
