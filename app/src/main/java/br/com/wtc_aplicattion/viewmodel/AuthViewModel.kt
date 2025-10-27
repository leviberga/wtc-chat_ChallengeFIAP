package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.TipoUsuario
import br.com.wtc_aplicattion.models.Usuario
import br.com.wtc_aplicattion.services.AuthService
import br.com.wtc_aplicattion.services.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar o estado de autenticação
 */
class AuthViewModel : ViewModel() {
    private val authService = AuthService()

    // Estado da autenticação
    var isLoggedIn by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<Usuario?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Estado dos campos de login
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
        isLoggedIn = authService.isUserLoggedIn()
        if (isLoggedIn) {
            val firebaseUser = authService.getCurrentUser()
            currentUser = firebaseUser?.let { createUserFromFirebase(it) }
        }
    }

    private fun createUserFromFirebase(firebaseUser: FirebaseUser): Usuario {
        val tipoUsuario = if (firebaseUser.email?.contains("operador") == true) {
            TipoUsuario.OPERADOR
        } else {
            TipoUsuario.CLIENTE
        }
        
        return Usuario(
            nome = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuário",
            tipo = tipoUsuario
        )
    }

    fun signIn() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email e senha são obrigatórios"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            when (val result = authService.signInWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    isLoggedIn = true
                    currentUser = result.user?.let { createUserFromFirebase(it) }
                    isLoading = false
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
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
            when (val result = authService.createUserWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    isLoggedIn = true
                    currentUser = result.user?.let { createUserFromFirebase(it) }
                    isLoading = false
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }
    fun signOut() {
        authService.signOut()
        isLoggedIn = false
        currentUser = null
        clearFields()
    }

    fun resetPassword() {
        if (email.isBlank()) {
            errorMessage = "Digite seu email para redefinir a senha"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            when (val result = authService.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    errorMessage = "Email de redefinição enviado!"
                    isLoading = false
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    fun toggleSignUpMode() {
        isSignUpMode = !isSignUpMode
        clearFields()
    }

    fun clearFields() {
        email = ""
        password = ""
        confirmPassword = ""
        errorMessage = null
    }

    fun clearError() {
        errorMessage = null
    }
}

