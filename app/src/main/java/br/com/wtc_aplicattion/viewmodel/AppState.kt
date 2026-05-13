package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.wtc_aplicattion.models.Campanha
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.models.TipoUsuario
import br.com.wtc_aplicattion.models.Usuario
import br.com.wtc_aplicattion.services.TokenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppState {

    var usuarioLogado by mutableStateOf<Usuario?>(null)

    var clientes = mutableStateListOf(
        Cliente(
            id = "1", nome = "João Silva", email = "joao@email.com",
            telefone = "11999990001", segmentId = null,
            tags = listOf("VIP", "Ativo"), score = 95.0,
            status = "ACTIVE", notas = "Cliente desde 2020"
        ),
        Cliente(
            id = "2", nome = "Maria Santos", email = "maria@email.com",
            telefone = "11999990002", segmentId = null,
            tags = listOf("Premium"), score = 88.0,
            status = "ACTIVE"
        ),
        Cliente(
            id = "3", nome = "Pedro Costa", email = "pedro@email.com",
            telefone = "11999990003", segmentId = null,
            tags = listOf("Regular"), score = 72.0,
            status = "INACTIVE", notas = "Interessado em eventos"
        )
    )

    var mensagens = mutableStateListOf<Mensagem>(
        Mensagem(
            id = "1", conversationId = "conv1", senderId = "operador",
            conteudo = "Olá! Como posso ajudar?", tipo = "TEXT",
            mediaUrl = null, deeplinkUrl = null,
            status = "READ", timestamp = getCurrentTime()
        ),
        Mensagem(
            id = "2", conversationId = "conv1", senderId = "cliente",
            conteudo = "Gostaria de saber sobre o próximo evento", tipo = "TEXT",
            mediaUrl = null, deeplinkUrl = null,
            status = "READ", timestamp = getCurrentTime()
        )
    )

    var campanhas = mutableStateListOf(
        Campanha(
            id = "1", title = "Evento Exclusivo WTC",
            content = "Participe do nosso evento de networking dia 25/10!",
            segmentId = null,
            deeplinkUrl = "https://wtc.com/evento",
            status = "SENT",
            scheduledAt = null,
            createdAt = "18/10/2025"
        )
    )

    var mostrarNotificacao by mutableStateOf(false)
    var notificacaoAtual by mutableStateOf<Campanha?>(null)

    fun isUserAuthenticated(): Boolean {
        return TokenManager.getToken() != null
    }

    fun logout() {
        TokenManager.clearToken()
        usuarioLogado = null
    }

    fun checkAuthState() {
        val token = TokenManager.getToken()
        if (token != null && usuarioLogado == null) {
            usuarioLogado = Usuario(
                nome = TokenManager.getEmail()?.substringBefore("@") ?: "Usuário",
                tipo = if (TokenManager.getRole() == "OPERATOR") TipoUsuario.OPERADOR else TipoUsuario.CLIENTE
            )
        } else if (token == null && usuarioLogado != null) {
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