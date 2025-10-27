package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.wtc_aplicattion.models.Campanha
import br.com.wtc_aplicattion.models.CampanhaAction
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.models.Usuario
import br.com.wtc_aplicattion.services.AuthService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppState {
    private val authService = AuthService()
    var usuarioLogado by mutableStateOf<Usuario?>(null)
    var clientes = mutableStateListOf(
        Cliente(
            1, "João Silva", "joao@email.com", "11999990001",
            listOf("VIP", "Ativo"), 95, "Ativo", "15/10/2025", "Cliente desde 2020"
        ),
        Cliente(
            2, "Maria Santos", "maria@email.com", "11999990002",
            listOf("Premium"), 88, "Ativo", "10/10/2025"
        ),
        Cliente(
            3, "Pedro Costa", "pedro@email.com", "11999990003",
            listOf("Regular"), 72, "Inativo", "20/09/2025", "Interessado em eventos"
        ),
        Cliente(
            4,
            "Ana Oliveira",
            "ana@email.com",
            "11999990004",
            listOf("VIP", "Premium"),
            98,
            "Ativo",
            "17/10/2025",
            "Preferência por eventos corporativos"
        ),
        Cliente(
            5, "Carlos Mendes", "carlos@email.com", "11999990005",
            listOf("Regular"), 65, "Ativo", "12/10/2025"
        ),
    )
    var mensagens = mutableStateListOf(
        Mensagem(1, 1, "operador", "Olá! Como posso ajudar?", getCurrentTime()),
        Mensagem(2, 1, "cliente", "Gostaria de saber sobre o próximo evento", getCurrentTime()),
        Mensagem(3, 1, "operador", "Temos um evento especial dia 25/10!", getCurrentTime(), true),
    )
    var campanhas = mutableStateListOf(
        Campanha(
            1, "Evento Exclusivo WTC",
            "Participe do nosso evento de networking dia 25/10!",
            "https://wtc.com/evento",
            listOf(
                CampanhaAction("btn1", "Inscrever-se", "https://wtc.com/inscricao"),
                CampanhaAction("btn2", "Saiba Mais", "https://wtc.com/detalhes")
            ),
            "VIP",
            "18/10/2025"
        )
    )

    var mostrarNotificacao by mutableStateOf(false)
    var notificacaoAtual by mutableStateOf<Campanha?>(null)

    fun isUserAuthenticated(): Boolean {
        return authService.isUserLoggedIn()
    }

    fun logout() {
        authService.signOut()
        usuarioLogado = null
    }

    fun checkAuthState() {
        if (authService.isUserLoggedIn() && usuarioLogado == null) {
            // Se há usuário logado no Firebase mas não no AppState, atualiza
            val firebaseUser = authService.getCurrentUser()
            if (firebaseUser != null) {
                // Determina o tipo de usuário baseado no email
                val tipoUsuario = if (firebaseUser.email?.contains("operador") == true) {
                    br.com.wtc_aplicattion.models.TipoUsuario.OPERADOR
                } else {
                    br.com.wtc_aplicattion.models.TipoUsuario.CLIENTE
                }
                
                usuarioLogado = Usuario(
                    nome = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuário",
                    tipo = tipoUsuario
                )
            }
        } else if (!authService.isUserLoggedIn() && usuarioLogado != null) {
            // Se não há usuário logado no Firebase mas há no AppState, limpa
            usuarioLogado = null
        }
    }

    companion object {
        fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}