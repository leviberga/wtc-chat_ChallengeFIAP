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
    @SerializedName("phone") val telefone: String?,
    val segmentId: String?,
    @SerializedName("segmentName") val segmentName: String?,
    val tags: List<String>?,
    val score: Double?,
    @SerializedName("customerStatus") val status: String,
    val notes: String? = null,
    val createdAt: String? = null
) {
    val tagsSeguras: List<String> get() = tags ?: emptyList()
    val scoreSeguro: Double get() = score ?: 0.0
}

data class Mensagem(
    val id: String,
    val conversationId: String,
    val senderId: String,
    @SerializedName("content") val conteudo: String,
    val messageType: String,
    val mediaUrl: String?,
    val deeplinkUrl: String?,
    val messageStatus: String,
    @SerializedName("createdAt") val timestamp: String,
    var importante: Boolean = false
)

/** Item retornado por GET /inbox/{customerId} (backend: InboxResponse) */
data class InboxItem(
    val conversationId: String,
    val customerId: String,
    val operatorId: String,
    val conversationStatus: String,
    val lastMessage: Mensagem?,
    val unreadCount: Long,
    val updatedAt: String?
)

data class Campanha(
    val id: String,
    val title: String,
    val content: String,
    val segmentId: String?,
    val targetCustomerIds: List<String>?,
    val deeplinkUrl: String?,
    @SerializedName("campaignStatus") val campaignStatus: String,
    val totalRecipients: Int,
    val scheduledAt: String?,
    val sentAt: String?,
    val createdAt: String?
)

data class Segmento(
    val id: String,
    val name: String,
    val description: String?,
    val customerCount: Long,
    val createdAt: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String? = null
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

data class MessageSendRequest(
    val customerId: String? = null,
    val segmentId: String? = null,
    val content: String,
    val messageType: String,
    val mediaUrl: String? = null,
    val deeplinkUrl: String? = null
)

data class CampaignCreateRequest(
    val title: String,
    val content: String,
    val segmentId: String? = null,
    val targetCustomerIds: List<String>? = null,
    val deeplinkUrl: String? = null,
    val scheduledAt: String? = null
)

data class CustomerUpdateRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val segmentId: String? = null,
    val tags: List<String>? = null,
    val score: Double? = null,
    val customerStatus: String? = null,
    val notes: String? = null
)

data class TimelineResponse(
    val customerId: String,
    val name: String,
    val email: String,
    val phone: String?,
    val segmentName: String?,
    val tags: List<String>?,
    val score: Double?,
    val customerStatus: String?,
    val createdAt: String?,
    val lastMessages: List<MessageSummary>?,
    val lastCampaigns: List<CampaignSummary>?,
    val openTasks: List<TaskSummary>?
) {
    data class MessageSummary(val id: String, val content: String, val sentAt: String?)
    data class CampaignSummary(val id: String, val title: String, val receivedAt: String?)
    data class TaskSummary(val id: String, val description: String, val status: String?)
}
