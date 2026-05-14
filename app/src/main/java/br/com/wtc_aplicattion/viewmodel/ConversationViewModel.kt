package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.Mensagem
import br.com.wtc_aplicattion.models.MessageSendRequest
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
                val convId = if (asOperator) {
                    val myEmail = TokenManager.getEmail()
                    inbox.firstOrNull { it.operatorId == myEmail }?.conversationId
                } else {
                    inbox.firstOrNull()?.conversationId
                }

                if (convId != null) {
                    conversationId = convId
                    val hist = api.getConversationMessages(token, convId)
                    if (hist.isSuccessful) {
                        mensagens = hist.body() ?: emptyList()
                    }
                    markOthersMessagesAsRead(token, myEmail = TokenManager.getEmail(), convId)
                } else {
                    conversationId = null
                    mensagens = emptyList()
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
                val convId = if (asOperator) {
                    inbox.firstOrNull { it.operatorId == myEmail }?.conversationId
                } else {
                    inbox.firstOrNull()?.conversationId
                }
                if (convId != null) {
                    conversationId = convId
                    val response = api.getConversationMessages(token, convId)
                    if (response.isSuccessful) {
                        mensagens = response.body() ?: emptyList()
                    }
                    markOthersMessagesAsRead(token, myEmail, convId)
                }
            } catch (_: Exception) { }
        }
    }

    private suspend fun markOthersMessagesAsRead(token: String, myEmail: String?, convId: String) {
        if (myEmail.isNullOrBlank()) return
        val list = mensagens
        list
            .filter { it.senderId != myEmail && it.messageStatus != "READ" }
            .forEach { m ->
                try {
                    api.updateMessageStatus(token, m.id, "READ")
                } catch (_: Exception) { }
            }
        val response = api.getConversationMessages(token, convId)
        if (response.isSuccessful) {
            mensagens = response.body() ?: mensagens
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
