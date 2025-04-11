package kz.lurker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kz.lurker.model.User
import kz.lurker.service.TokenService

class MainActivity : AppCompatActivity() {

    private lateinit var tokenService: TokenService

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private lateinit var userInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tokenService = TokenService(application)

        userInfoTextView = findViewById(R.id.userInfoTextView)
        getUserInfo()
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            try {
                val response = client.get("http://10.0.2.2:8081/user/getUser") {
                     headers.append(HttpHeaders.Authorization, "Bearer ${tokenService.getToken()}")
                }
                if (response.status == HttpStatusCode.OK) {
                    val userInfo = response.body<User>()
                    showError("Success")
                    displayUserInfo(userInfo)
                } else {
                    throw Exception("Failed to get user data: ${response.status}")
                    showError("Failed to get user data: ${response.status}")
                }
            } catch (e: Exception) {
                throw e
                showError("Error: ${e.message}")
            }
        }
    }

    private fun displayUserInfo(user: User) {
        userInfoTextView.text = "GPA: ${user.gpa}\nRole: ${user.role}\nName: ${user.firstName} ${user.lastName}"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}