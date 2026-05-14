package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.CustomerUpdateRequest
import br.com.wtc_aplicattion.models.Segmento
import br.com.wtc_aplicattion.models.TimelineResponse
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class CustomerDetailViewModel : ViewModel() {

    var cliente by mutableStateOf<Cliente?>(null)
        private set

    var timeline by mutableStateOf<TimelineResponse?>(null)
        private set

    var segmentos by mutableStateOf<List<Segmento>>(emptyList())
        private set

    /** Inicia true: o 1º frame da rota de perfil mostra loading até [load] rodar (evita navigateUp prematuro). */
    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    private val api get() = RetrofitClient.instance

    fun load(customerId: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    errorMessage = "Sessão expirada"
                    isLoading = false
                    return@launch
                }
                val c = api.getCustomerById(token, customerId)
                val t = api.getTimeline(token, customerId)
                if (c.isSuccessful) {
                    val body = c.body()
                    cliente = body
                    if (body == null) errorMessage = "Cliente não encontrado"
                }
                if (t.isSuccessful) timeline = t.body()
                if (!c.isSuccessful) errorMessage = "Cliente não encontrado"
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadSegments() {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val r = api.getSegments(token)
                if (r.isSuccessful) {
                    segmentos = r.body() ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Atualiza tags e segmento do cliente (PUT /customers/{id}).
     */
    fun saveCrmFields(
        customerId: String,
        tags: List<String>,
        segmentId: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val c = cliente ?: run {
            onResult(false, "Cliente não carregado")
            return
        }
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    isSaving = false
                    onResult(false, "Sessão expirada")
                    return@launch
                }
                val body = CustomerUpdateRequest(
                    name = c.nome,
                    email = c.email,
                    phone = c.telefone,
                    segmentId = segmentId,
                    tags = tags,
                    score = c.scoreSeguro,
                    customerStatus = c.status,
                    notes = c.notes
                )
                val r = api.updateCustomer(token, customerId, body)
                if (r.isSuccessful) {
                    load(customerId)
                    loadSegments()
                    onResult(true, null)
                } else {
                    val msg = r.errorBody()?.string()?.take(200) ?: "Erro ao salvar"
                    errorMessage = msg
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onResult(false, e.message)
            } finally {
                isSaving = false
            }
        }
    }
}
