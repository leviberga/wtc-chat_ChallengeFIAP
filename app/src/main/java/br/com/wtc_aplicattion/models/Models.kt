package br.com.wtc_aplicattion.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    val nome: String,
    val tipo: TipoUsuario
)

enum class TipoUsuario {
    OPERADOR, CLIENTE
}
data class Cliente(
    val id: String,
    @SerializedName("name") val nome: String,
    val email: String,
    @SerializedName("phone") val telefone: String,
    val segmentId: String?,
    val tags: List<String>,
    val score: Double,
    @SerializedName("customerStatus") val status: String,
    var notas: String? = null
)

data class Mensagem(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val conteudo: String,
    val tipo: String,
    val mediaUrl: String?,
    val deeplinkUrl: String?,
    val status: String,
    val timestamp: String,
    val remetente: String = senderId,
    var importante: Boolean = false,
    val clienteId: String = conversationId
)

data class Campanha(
    val id: String,
    val title: String,
    val content: String,
    val segmentId: String?,
    val deeplinkUrl: String?,
    val status: String,
    val scheduledAt: String?,
    val createdAt: String
)

data class Segmento(
    val id: String,
    val name: String,
    val description: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val type: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String
)