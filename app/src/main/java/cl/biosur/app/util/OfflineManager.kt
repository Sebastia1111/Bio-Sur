package cl.biosur.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cl.biosur.app.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Monitor de conectividad para el modo offline (campo 7).
 *
 *
 * Uso:
 *   OfflineManager.init(context)
 *   OfflineManager.isOnline.observe(lifecycleOwner) { online -> ... }
 */
object OfflineManager {

    private val _isOnline = MutableLiveData(true)
    val isOnline: LiveData<Boolean> = _isOnline

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { _isOnline.postValue(true) }
        override fun onLost(network: Network) { _isOnline.postValue(false) }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            _isOnline.postValue(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        }
    }

    fun init(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, callback)

            // Estado inicial
            val active = cm.activeNetwork
            _isOnline.value = active != null && cm
                .getNetworkCapabilities(active)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (t: Throwable) {
            _isOnline.postValue(true)
        }
    }

    /**
     * Marca un informe como "synced". En esta versión local-only, "synced" significa
     * que ya fue exportado a PDF/JSON por el supervisor.
     */
    fun markAsExported(
        reportId: Long,
        onDone: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            ServiceLocator.reportRepository.markSynced(reportId)
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }
}
