package kz.lurker.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val message: String, val status: String, val token: String)

interface ApiService {

    @POST("/api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

}