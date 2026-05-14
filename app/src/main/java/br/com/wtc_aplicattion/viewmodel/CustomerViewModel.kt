package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {

    var clientes by mutableStateOf<List<Cliente>>(emptyList())
        private set

    /** Cliente carregado por ID (chat / detalhe). */
    var clienteSelecionado by mutableStateOf<Cliente?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val api get() = RetrofitClient.instance

    fun loadClientePorId(id: String) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val r = api.getCustomerById(token, id)
                if (r.isSuccessful) clienteSelecionado = r.body()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    /**
     * Associa o usuário logado (email) ao registro de cliente no CRM, se existir.
     */
    fun resolveLoggedInCustomerId(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    onResult(null)
                    return@launch
                }
                val email = TokenManager.getEmail() ?: run {
                    onResult(null)
                    return@launch
                }
                val r = api.getCustomers(token)
                val id = if (r.isSuccessful) {
                    r.body()?.firstOrNull { it.email.equals(email, ignoreCase = true) }?.id
                } else null
                onResult(id)
            } catch (_: Exception) {
                onResult(null)
            }
        }
    }

    fun loadClientes() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val response = api.getCustomers(token)
                if (response.isSuccessful) {
                    clientes = response.body() ?: emptyList()
                } else {
                    errorMessage = "Erro ao carregar clientes"
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
                android.util.Log.e("CustomerViewModel", "Erro: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun loadClientesPorSegmento(segmentId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val response = api.getCustomers(token, segmentId = segmentId)
                if (response.isSuccessful) {
                    clientes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadClientesPorTag(tag: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: return@launch
                val response = api.getCustomers(token, tag = tag)
                if (response.isSuccessful) {
                    clientes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}