package cl.biosur.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.biosur.app.ServiceLocator
import cl.biosur.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de login.
 * Responsabilidad: Compañero 1.
 */
class LoginViewModel : ViewModel() {

    private val repo: AuthRepository = ServiceLocator.authRepository

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState.Error("Completa todos los campos")
            return
        }
        _state.value = LoginState.Loading
        viewModelScope.launch {
            when (val r = repo.login(email, password)) {
                is AuthRepository.LoginResult.Success -> _state.value = LoginState.Success(r.user.role)
                is AuthRepository.LoginResult.Error -> _state.value = LoginState.Error(r.message)
            }
        }
    }

    fun reset() { _state.value = LoginState.Idle }

    sealed interface LoginState {
        data object Idle : LoginState
        data object Loading : LoginState
        data class Success(val role: String) : LoginState
        data class Error(val message: String) : LoginState
    }
}
