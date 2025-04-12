package kz.lurker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kz.lurker.service.TokenService

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenService = TokenService(application)

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

class LoginViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(application) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
