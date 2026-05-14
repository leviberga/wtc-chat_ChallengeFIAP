package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.Cliente
import br.com.wtc_aplicattion.models.TimelineResponse
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class CustomerDetailViewModel : ViewModel() {

    var cliente by mutableStateOf<Cliente?>(null)
        private set

    var timeline by mutableStateOf<TimelineResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val api get() = RetrofitClient.instance

    fun load(customerId: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    isLoading = false
                    return@launch
                }
                val c = api.getCustomerById(token, customerId)
                val t = api.getTimeline(token, customerId)
                if (c.isSuccessful) cliente = c.body()
                if (t.isSuccessful) timeline = t.body()
                if (!c.isSuccessful) errorMessage = "Cliente não encontrado"
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
