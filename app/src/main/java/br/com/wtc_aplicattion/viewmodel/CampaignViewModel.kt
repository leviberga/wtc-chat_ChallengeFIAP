package br.com.wtc_aplicattion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.wtc_aplicattion.models.Campanha
import br.com.wtc_aplicattion.models.CampaignCreateRequest
import br.com.wtc_aplicattion.models.Segmento
import br.com.wtc_aplicattion.services.RetrofitClient
import br.com.wtc_aplicattion.services.TokenManager
import kotlinx.coroutines.launch

class CampaignViewModel : ViewModel() {

    var campanhas by mutableStateOf<List<Campanha>>(emptyList())
        private set

    var segmentos by mutableStateOf<List<Segmento>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val api get() = RetrofitClient.instance

    fun loadCampaignsAndSegments() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val token = TokenManager.getBearerToken() ?: run {
                    isLoading = false
                    return@launch
                }
                val c = api.getCampaigns(token)
                val s = api.getSegments(token)
                if (c.isSuccessful) campanhas = c.body() ?: emptyList()
                if (s.isSuccessful) segmentos = s.body() ?: emptyList()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun createCampaign(
        title: String,
        content: String,
        segmentId: String?,
        deeplinkUrl: String?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val seg = segmentId?.trim()?.takeIf { it.isNotBlank() }
                if (seg == null) {
                    errorMessage = "Selecione um segmento de destino"
                    return@launch
                }
                errorMessage = null
                val token = TokenManager.getBearerToken() ?: return@launch
                val body = CampaignCreateRequest(
                    title = title,
                    content = content,
                    segmentId = seg,
                    targetCustomerIds = null,
                    deeplinkUrl = deeplinkUrl?.trim()?.takeIf { it.isNotBlank() },
                    scheduledAt = null
                )
                val response = api.createCampaign(token, body)
                if (response.isSuccessful) {
                    loadCampaignsAndSegments()
                    onDone()
                } else {
                    errorMessage = response.errorBody()?.string()?.take(200) ?: "Erro ao criar campanha"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun pickLatestForCustomer(customerId: String?, segmentId: String?): Campanha? {
        if (customerId == null) return null
        return campanhas
            .filter { it.campaignStatus == "SENT" }
            .filter { camp ->
                when {
                    camp.targetCustomerIds?.contains(customerId) == true -> true
                    camp.segmentId != null && camp.segmentId == segmentId -> true
                    else -> false
                }
            }
            .maxByOrNull { camp ->
                listOf(camp.sentAt, camp.createdAt).filterNotNull().maxOrNull().orEmpty()
            }
    }
}
