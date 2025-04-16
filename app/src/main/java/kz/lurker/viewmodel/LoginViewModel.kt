package kz.lurker.viewmodel

import android.app.Application
import android.util.Log
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
    companion object {
        private const val TAG = "LoginVM"
    }

    private val tokenService = TokenService(application)

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> = _loginResult

    fun login(username: String, password: String) {
        Log.d(TAG, "login() called with username='$username'")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Building HTTP client and request...")
                val response = client.post("http://192.168.1.35:8081/user/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username, password))
                }

                Log.d(TAG, "Request sent. Received HTTP status: ${response.status}")

                if (response.status == HttpStatusCode.OK) {
                    Log.i(TAG, "Status OK — parsing body")
                    val responseBody = response.body<LoginResponse>()
                    Log.i(TAG, "Parsed response: message='${responseBody.message}', status='${responseBody.status}', token='${responseBody.token.take(10)}…'")

                    Log.d(TAG, "Saving token to secure prefs")
                    tokenService.saveToken(responseBody.token)

                    _loginResult.value = "Login successful"
                    Log.i(TAG, "LiveData _loginResult set to 'Login successful'")
                } else {
                    Log.w(TAG, "Login failed with status ${response.status}")
                    _loginResult.value = "Login failed: ${response.status}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during login: ${e.message}", e)
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
