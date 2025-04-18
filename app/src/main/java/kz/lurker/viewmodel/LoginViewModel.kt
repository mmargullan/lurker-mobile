package kz.lurker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kz.lurker.service.TokenService

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenService = TokenService(application)
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Application.MODE_PRIVATE)

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = client.post("https://test-student-forum.serveo.net/api/auth-api/user/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username, password))
                }


                if (response.status == HttpStatusCode.OK) {
                    val responseBody = response.body<LoginResponse>()
                    tokenService.saveToken(responseBody.token)

                    _loginResult.value = "Login successful"
                    sharedPreferences.edit().putBoolean("isLogged", true).apply()
                } else {
                    _loginResult.value = "Login failed: ${response.status}"
                }
            } catch (e: Exception) {
                _loginResult.value = "Error: ${e.message}"
            }
        }
    }

    @Serializable
    data class LoginRequest(val login: String, val password: String)

    @Serializable
    data class LoginResponse(val message: String, val status: String, val token: String)
}
