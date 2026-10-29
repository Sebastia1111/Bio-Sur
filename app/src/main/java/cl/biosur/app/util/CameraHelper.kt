package cl.biosur.app.util

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor

/**
 * Helper de CameraX para capturar fotos (campo 2: imágenes/videos).
 *
 *
 *
 * Uso:
 *   val helper = CameraHelper(this, previewView)
 *   helper.bind()
 *   helper.takePhoto { savedFile -> ... }
 *
 * Para video, en el esqueleto se puede usar androidx.camera.video.Recorder
 * (ver TODO abajo).
 */
class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {
    private var imageCapture: ImageCapture? = null
    private val executor: Executor = ContextCompat.getMainExecutor(context)

    fun bind() {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                // TODO Compañero 3: manejar error (no hay cámara, permisos, etc.)
                e.printStackTrace()
            }
        }, executor)
    }

    fun takePhoto(onSaved: (File) -> Unit, onError: (Throwable) -> Unit = {}) {
        val capture = imageCapture ?: return onError(IllegalStateException("ImageCapture null"))
        val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
        val file = File(mediaDir, "IMG_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSaved(file)
                }
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun unbind() {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            providerFuture.get().unbindAll()
        }, executor)
    }
}
