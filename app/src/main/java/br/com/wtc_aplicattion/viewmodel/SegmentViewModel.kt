package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.SegmentRequestBody
import br.com.wtc_aplicattion.models.Segmento
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class SegmentViewModel : ViewModel() {

    var segmentos by mutableStateOf<List<Segmento>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val api get() = RetrofitClient.instance

    fun load() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    isLoading = false
                    return@launch
                }
                val r = api.getSegments(token)
                if (r.isSuccessful) {
                    segmentos = r.body() ?: emptyList()
                } else {
                    errorMessage = "Erro ao carregar segmentos"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun create(name: String, description: String?, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    onDone(false)
                    return@launch
                }
                val r = api.createSegment(
                    token,
                    SegmentRequestBody(name = name.trim(), description = description?.trim()?.takeIf { it.isNotEmpty() })
                )
                if (r.isSuccessful) {
                    load()
                    onDone(true)
                } else {
                    errorMessage = r.errorBody()?.string()?.take(150) ?: "Erro ao criar"
                    onDone(false)
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onDone(false)
            }
        }
    }

    fun delete(id: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    onDone(false)
                    return@launch
                }
                val r = api.deleteSegment(token, id)
                if (r.isSuccessful) {
                    load()
                    onDone(true)
                } else {
                    errorMessage = r.errorBody()?.string()?.take(150) ?: "Erro ao excluir"
                    onDone(false)
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onDone(false)
            }
        }
    }
}
