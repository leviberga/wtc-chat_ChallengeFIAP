package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.LoginRequest
import br.com.wtc_aplicattion.models.TipoUsuario
import br.com.wtc_aplicattion.models.Usuario
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<Usuario?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var isSignUpMode by mutableStateOf(false)
        private set

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val token = TokenManager.getToken()
        if (token != null) {
            isLoggedIn = true
            currentUser = Usuario(
                nome = TokenManager.getEmail() ?: "Usuário",
                tipo = if (TokenManager.getRole() == "OPERATOR") TipoUsuario.OPERADOR else TipoUsuario.CLIENTE
            )
        }
    }

    fun signIn() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email e senha são obrigatórios"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    TokenManager.saveToken(body.token, body.refreshToken, body.email, body.role)
                    currentUser = Usuario(
                        nome = body.name,
                        tipo = if (body.role == "OPERATOR") TipoUsuario.OPERADOR else TipoUsuario.CLIENTE
                    )
                    isLoggedIn = true
                } else {
                    errorMessage = "Email ou senha inválidos"
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signUp() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email e senha são obrigatórios"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "As senhas não coincidem"
            return
        }

        if (password.length < 6) {
            errorMessage = "A senha deve ter pelo menos 6 caracteres"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = api.register(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    TokenManager.saveToken(body.token, body.refreshToken, body.email, body.role)
                    currentUser = Usuario(
                        nome = body.name,
                        tipo = if (body.role == "OPERATOR") TipoUsuario.OPERADOR else TipoUsuario.CLIENTE
                    )
                    isLoggedIn = true
                } else {
                    errorMessage = "Erro ao criar conta"
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signOut() {
        TokenManager.clearToken()
        isLoggedIn = false
        currentUser = null
        clearFields()
    }

    fun updateEmail(newEmail: String) { email = newEmail }
    fun updatePassword(newPassword: String) { password = newPassword }
    fun updateConfirmPassword(newConfirmPassword: String) { confirmPassword = newConfirmPassword }
    fun toggleSignUpMode() { isSignUpMode = !isSignUpMode; clearFields() }
    fun clearFields() { email = ""; password = ""; confirmPassword = ""; errorMessage = null }
    fun clearError() { errorMessage = null }
}