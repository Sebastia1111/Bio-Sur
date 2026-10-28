package cl.biosur.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import cl.biosur.app.R
import cl.biosur.app.databinding.FragmentLoginBinding
import cl.biosur.app.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Pantalla de login.
 * Responsabilidad: Compañero 1.
 *
 * Cuentas demo:
 *   - Empresa:    admin@biosur.cl      / biosur2024
 *   - Supervisor: supervisor@biosur.cl / biosur2024
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya hay sesión iniciada, redirigir automáticamente según el rol
        if (SessionManager.isLoggedIn()) {
            navigateAccordingToRole()
            return
        }

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.emailInput.text.toString(),
                binding.passwordInput.text.toString()
            )
        }

        // Acceso rápido tocando el texto demo
        binding.demoHint.setOnClickListener {
            if (binding.emailInput.text.isNullOrBlank()) {
                binding.emailInput.setText("admin@biosur.cl")
                binding.passwordInput.setText("biosur2024")
            } else if (binding.emailInput.text.toString() == "admin@biosur.cl") {
                binding.emailInput.setText("supervisor@biosur.cl")
                binding.passwordInput.setText("biosur2024")
            } else {
                binding.emailInput.setText("admin@biosur.cl")
                binding.passwordInput.setText("biosur2024")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Idle -> Unit
                        is LoginViewModel.LoginState.Loading -> {
                            binding.errorText.visibility = View.GONE
                            binding.loginButton.isEnabled = false
                            binding.loginButton.text = "Ingresando…"
                        }
                        is LoginViewModel.LoginState.Success -> {
                            binding.loginButton.isEnabled = true
                            binding.loginButton.text = getString(R.string.login_button)
                            navigateAccordingToRole()
                        }
                        is LoginViewModel.LoginState.Error -> {
                            binding.loginButton.isEnabled = true
                            binding.loginButton.text = getString(R.string.login_button)
                            binding.errorText.text = state.message
                            binding.errorText.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun navigateAccordingToRole() {
        if (SessionManager.isCompany()) {
            findNavController().navigate(R.id.action_login_to_dashboard)
        } else {
            findNavController().navigate(R.id.action_login_to_reports)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
