package br.com.wtc_aplicattion.services

import br.com.wtc_aplicattion.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): Response<AuthResponse>

    // Customers
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

    // Messages
    @POST("messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<List<Mensagem>>

    @GET("inbox/{customerId}")
    suspend fun getInbox(
        @Header("Authorization") token: String,
        @Path("customerId") customerId: String
    ): Response<List<Any>>

    // Campaigns
    @GET("campaigns")
    suspend fun getCampaigns(
        @Header("Authorization") token: String
    ): Response<List<Campanha>>

    @POST("campaigns")
    suspend fun createCampaign(
        @Header("Authorization") token: String,
        @Body campaign: Map<String, Any>
    ): Response<Campanha>

    // Segments
    @GET("segments")
    suspend fun getSegments(
        @Header("Authorization") token: String
    ): Response<List<Segmento>>
}