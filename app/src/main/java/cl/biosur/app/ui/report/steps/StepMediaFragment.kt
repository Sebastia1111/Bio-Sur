package cl.biosur.app.ui.report.steps

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.databinding.StepMediaBinding
import cl.biosur.app.ui.report.MediaAdapter
import cl.biosur.app.ui.report.ReportViewModel
import cl.biosur.app.util.LocationBus
import cl.biosur.app.util.LocationService
import cl.biosur.app.util.Permissions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

/**
 * Paso 2: Multimedia + ubicación GPS.
 * Responsabilidad: Compañero 2 (UI), apoyado por helpers del Compañero 3.
 */
class StepMediaFragment : Fragment() {

    private var _binding: StepMediaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by activityViewModels()
    private lateinit var perms: Permissions
    private lateinit var mediaAdapter: MediaAdapter

    private var currentPhotoFile: File? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoFile != null && currentPhotoFile!!.exists()) {
            viewModel.addMedia(
                MediaEntity(
                    reportId = 0,
                    type = "IMAGE",
                    filePath = currentPhotoFile!!.absolutePath,
                    comment = "Foto tomada en terreno (${currentPhotoFile!!.name})"
                )
            )
            Snackbar.make(binding.root, "Foto agregada con éxito", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.addMedia(
                MediaEntity(
                    reportId = 0,
                    type = "IMAGE",
                    filePath = it.toString(),
                    comment = "Imagen seleccionada de la galería"
                )
            )
            Snackbar.make(binding.root, "Imagen de galería añadida", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = StepMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        perms = Permissions(this) { granted ->
            if (granted[Manifest.permission.CAMERA] == true ||
                granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // Permisos otorgados
            }
        }
        if (!perms.isCameraGranted() || !perms.isFineLocationGranted()) {
            perms.requestCameraAndLocation()
        }

        mediaAdapter = MediaAdapter { media -> viewModel.removeMedia(media) }
        binding.mediaRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.mediaRecycler.adapter = mediaAdapter

        binding.btnTakePhoto.setOnClickListener {
            launchCamera()
        }

        binding.btnPickGallery.setOnClickListener {
            pickGalleryLauncher.launch("image/*")
        }

        binding.btnCaptureLocation.setOnClickListener {
            LocationService.start(requireContext())
            viewLifecycleOwner.lifecycleScope.launch {
                LocationBus.lastLocation.collect { loc ->
                    loc?.let {
                        viewModel.setLocation(it.latitude, it.longitude)
                        binding.locationText.text = "Ubicación: %.5f, %.5f".format(it.latitude, it.longitude)
                    }
                }
            }
        }

        // Si ya había ubicación capturada en el borrador, restaurarla en la vista
        viewModel.draftLocation.value?.let { (lat, lon) ->
            binding.locationText.text = "Ubicación: %.5f, %.5f".format(lat, lon)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.draftMedia.collect { list ->
                    mediaAdapter.submitList(list)
                    binding.mediaEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun launchCamera() {
        val mediaDir = File(requireContext().filesDir, "media").apply { mkdirs() }
        val photoFile = File(mediaDir, "IMG_${System.currentTimeMillis()}.jpg")
        currentPhotoFile = photoFile
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePhotoLauncher.launch(photoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocationService.stop(requireContext())
        _binding = null
    }
}
