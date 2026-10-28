package cl.biosur.app.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import cl.biosur.app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Servicio de ubicación en primer plano.
 *
 * Responsabilidad del COMPAÑERO 3.
 *
 * - Se inicia cuando el supervisor abre un formulario de informe.
 * - Publica la última ubicación en [LocationBus] (StateFlow global).
 * - Se detiene cuando se cierra el formulario o se guarda el informe.
 *
 * Para usarlo:
 *   LocationService.start(requireContext())
 *   LocationBus.lastLocation.collect { loc -> ... }
 *   LocationService.stop(requireContext())
 */
class LocationService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let(LocationBus::emit)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createChannels()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
            .setMinUpdateIntervalMillis(INTERVAL_MS)
            .build()
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        return START_STICKY
    }

    override fun onDestroy() {
        if (this::fusedClient.isInitialized) {
            fusedClient.removeLocationUpdates(callback)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_location_text))
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_location),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.notif_channel_location) }
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "biosur_location"
        private const val NOTIFICATION_ID = 1001
        private const val INTERVAL_MS = 10_000L  // 10 s

        fun start(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationService::class.java))
        }
    }
}

/**
 * Bus de ubicación observable desde cualquier parte de la app.
 */
object LocationBus {
    private val _lastLocation = kotlinx.coroutines.flow.MutableStateFlow<Location?>(null)
    val lastLocation: kotlinx.coroutines.flow.StateFlow<Location?> = _lastLocation

    fun emit(location: Location) {
        _lastLocation.value = location
    }
}
