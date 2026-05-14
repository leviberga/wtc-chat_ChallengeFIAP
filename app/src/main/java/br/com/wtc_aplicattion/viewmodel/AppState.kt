package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.wtc_aplicattion.models.Campanha
import br.com.wtc_aplicattion.models.TipoUsuario
import br.com.wtc_aplicattion.models.Usuario
import br.com.wtc_aplicattion.services.TokenManager

/**
 * Estado UI compartilhado (sem dados mock — listas vêm dos ViewModels + API).
 */
class AppState {

    var usuarioLogado by mutableStateOf<Usuario?>(null)

    /** Campanha exibida no popup in-app (cliente), preenchida após GET /campaigns. */
    var campanhaPopup by mutableStateOf<Campanha?>(null)
    var mostrarCampanhaPopup by mutableStateOf(false)

    fun isUserAuthenticated(): Boolean = TokenManager.getToken() != null

    fun logout() {
        TokenManager.clearToken()
        usuarioLogado = null
        campanhaPopup = null
        mostrarCampanhaPopup = false
    }

    fun checkAuthState() {
        val token = TokenManager.getToken()
        if (token != null && usuarioLogado == null) {
            usuarioLogado = Usuario(
                nome = TokenManager.getDisplayName()
                    ?: TokenManager.getEmail()?.substringBefore("@")
                    ?: "Usuário",
                tipo = if (TokenManager.getRole() == "OPERATOR") TipoUsuario.OPERADOR else TipoUsuario.CLIENTE
            )
        } else if (token == null && usuarioLogado != null) {
            usuarioLogado = null
        }
    }

    fun dismissCampanhaPopup() {
        mostrarCampanhaPopup = false
        campanhaPopup = null
    }
}
