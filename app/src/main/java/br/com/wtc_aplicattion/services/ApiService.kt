package br.com.wtc_aplicattion.services

import br.com.wtc_aplicattion.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("customers")
    suspend fun getCustomers(
        @Header("Authorization") token: String,
        @Query("segmentId") segmentId: String? = null,
        @Query("tag") tag: String? = null
    ): Response<List<Cliente>>

    @GET("customers/{id}")
    suspend fun getCustomerById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Cliente>

    @GET("customers/{id}/timeline")
    suspend fun getTimeline(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<TimelineResponse>

    @PUT("customers/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: CustomerUpdateRequest
    ): Response<Cliente>

    @GET("conversations/{conversationId}/messages")
    suspend fun getConversationMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: String
    ): Response<List<Mensagem>>

    @POST("messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: MessageSendRequest
    ): Response<List<Mensagem>>

    @GET("messages/{id}")
    suspend fun getMessageById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Mensagem>

    @PATCH("messages/{id}/status")
    suspend fun updateMessageStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("status") status: String
    ): Response<Mensagem>

    @GET("inbox/{customerId}")
    suspend fun getInbox(
        @Header("Authorization") token: String,
        @Path("customerId") customerId: String
    ): Response<List<InboxItem>>

    @GET("campaigns")
    suspend fun getCampaigns(
        @Header("Authorization") token: String
    ): Response<List<Campanha>>

    @GET("campaigns/{id}")
    suspend fun getCampaignById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Campanha>

    @POST("campaigns")
    suspend fun createCampaign(
        @Header("Authorization") token: String,
        @Body body: CampaignCreateRequest
    ): Response<Campanha>

    @GET("segments")
    suspend fun getSegments(
        @Header("Authorization") token: String
    ): Response<List<Segmento>>

    @POST("segments")
    suspend fun createSegment(
        @Header("Authorization") token: String,
        @Body body: SegmentRequestBody
    ): Response<Segmento>

    @PUT("segments/{id}")
    suspend fun updateSegment(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: SegmentRequestBody
    ): Response<Segmento>

    @DELETE("segments/{id}")
    suspend fun deleteSegment(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
