package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.InboxItem
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.models.MessageSendRequest
import br.com.wtc_aplicattion.services.ApiService
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConversationViewModel : ViewModel() {

    var mensagens by mutableStateOf<List<Mensagem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    /** Conversa ativa operador↔cliente; na visão cliente fica null (feed agregado). */
    var conversationId by mutableStateOf<String?>(null)
        private set

    private val api get() = RetrofitClient.instance

    private var pollJob: Job? = null

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun startPolling(customerId: String, asOperator: Boolean) {
        stopPolling()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                refreshMensagens(customerId, asOperator)
            }
        }
    }

    fun loadMensagens(customerId: String, asOperator: Boolean) {
        errorMessage = null
        viewModelScope.launch {
            isLoading = true
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    isLoading = false
                    return@launch
                }
                val inboxRes = api.getInbox(token, customerId)
                if (!inboxRes.isSuccessful) {
                    errorMessage = "Erro ao carregar inbox"
                    isLoading = false
                    return@launch
                }
                val inbox = inboxRes.body() ?: emptyList()

                if (asOperator) {
                    val myEmail = TokenManager.getEmail()
                    val convId = inbox.firstOrNull { it.operatorId == myEmail }?.conversationId
                    if (convId != null) {
                        conversationId = convId
                        val hist = api.getConversationMessages(token, convId)
                        if (hist.isSuccessful) {
                            mensagens = hist.body() ?: emptyList()
                        }
                        markOthersMessagesAsRead(
                            token,
                            TokenManager.getEmail(),
                            customerId,
                            convId,
                            asOperator = true
                        )
                    } else {
                        conversationId = null
                        mensagens = emptyList()
                    }
                } else {
                    conversationId = null
                    mensagens = fetchMergedClientMessages(api, token, inbox)
                    markOthersMessagesAsRead(
                        token,
                        TokenManager.getEmail(),
                        customerId,
                        convId = null,
                        asOperator = false
                    )
                }
            } catch (e: Exception) {
                errorMessage = "Erro: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshMensagens(customerId: String, asOperator: Boolean) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val inboxRes = api.getInbox(token, customerId)
                if (!inboxRes.isSuccessful) return@launch
                val inbox = inboxRes.body() ?: emptyList()
                val myEmail = TokenManager.getEmail()

                if (asOperator) {
                    val convId = inbox.firstOrNull { it.operatorId == myEmail }?.conversationId
                    if (convId != null) {
                        conversationId = convId
                        val response = api.getConversationMessages(token, convId)
                        if (response.isSuccessful) {
                            mensagens = response.body() ?: emptyList()
                        }
                        markOthersMessagesAsRead(token, myEmail, customerId, convId, asOperator = true)
                    }
                } else {
                    conversationId = null
                    mensagens = fetchMergedClientMessages(api, token, inbox)
                    markOthersMessagesAsRead(token, myEmail, customerId, convId = null, asOperator = false)
                }
            } catch (_: Exception) { }
        }
    }

    private suspend fun fetchMergedClientMessages(
        api: ApiService,
        token: String,
        inbox: List<InboxItem>
    ): List<Mensagem> {
        val merged = mutableListOf<Mensagem>()
        for (item in inbox) {
            val r = api.getConversationMessages(token, item.conversationId)
            if (r.isSuccessful) {
                merged.addAll(r.body() ?: emptyList())
            }
        }
        return merged
            .distinctBy { it.id }
            .sortedWith(compareBy({ it.timestamp }, { it.id }))
    }

    private suspend fun markOthersMessagesAsRead(
        token: String,
        myEmail: String?,
        customerId: String,
        convId: String?,
        asOperator: Boolean
    ) {
        if (myEmail.isNullOrBlank()) return
        mensagens
            .filter { it.senderId != myEmail && it.messageStatus != "READ" }
            .forEach { m ->
                try {
                    api.updateMessageStatus(token, m.id, "READ")
                } catch (_: Exception) { }
            }
        if (asOperator && convId != null) {
            val response = api.getConversationMessages(token, convId)
            if (response.isSuccessful) {
                mensagens = response.body() ?: mensagens
            }
        } else if (!asOperator) {
            val inboxRes = api.getInbox(token, customerId)
            if (!inboxRes.isSuccessful) return
            val inbox = inboxRes.body() ?: return
            mensagens = fetchMergedClientMessages(api, token, inbox)
        }
    }

    fun enviarMensagem(customerId: String, conteudo: String, asOperator: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val body = MessageSendRequest(
                    customerId = customerId,
                    segmentId = null,
                    content = conteudo,
                    messageType = "TEXT",
                    mediaUrl = null,
                    deeplinkUrl = null
                )
                val response = api.sendMessage(token, body)
                if (response.isSuccessful) {
                    refreshMensagens(customerId, asOperator)
                    onSuccess()
                } else {
                    val err = response.errorBody()?.string()
                    errorMessage = err?.take(200) ?: "Falha ao enviar mensagem"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao enviar: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
