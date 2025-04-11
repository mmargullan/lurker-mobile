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
import kz.lurker.model.Group
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

        val user = getUserInfoFromPrefs()
        if (user != null) {
            displayUserInfo(user)
        } else {
            getUserInfo()
        }

    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            try {
                val response = client.get("http://10.0.2.2:8081/user/getUser") {
                     headers.append(HttpHeaders.Authorization, "Bearer ${tokenService.getToken()}")
                }
                if (response.status == HttpStatusCode.OK) {
                    val userInfo = response.body<User>()
                    saveUser(userInfo)
                    displayUserInfo(userInfo)
                } else {
                    throw Exception("Failed to get user data: ${response.status}")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun displayUserInfo(user: User) {
        userInfoTextView.text = "GPA: ${user.gpa}\nRole: ${user.role}\nName: ${user.firstName} ${user.lastName}"
    }

    private fun saveUser(user: User) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("login", user.login)
        editor.putString("password", user.password)
        editor.putString("role", user.role)
        editor.putString("firstName", user.firstName)
        editor.putString("lastName", user.lastName)
        editor.putFloat("gpa", user.gpa.toFloat())
        editor.putString("phone", user.phone)
        editor.putInt("courseNumber", user.courseNumber)
        editor.putString("education", user.education)
        editor.putString("address", user.address)
        editor.putString("birthDate", user.birthDate)
        editor.putString("groupName", user.group.name)
        editor.putFloat("groupAverageGpa", user.group.averageGpa.toFloat())
        editor.putInt("groupStudentCount", user.group.studentCount)

        editor.apply()
    }

    private fun getUserInfoFromPrefs(): User? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val login = sharedPreferences.getString("login", null)
        val password = sharedPreferences.getString("password", null)
        val role = sharedPreferences.getString("role", null)
        val firstName = sharedPreferences.getString("firstName", null)
        val lastName = sharedPreferences.getString("lastName", null)
        val gpa = sharedPreferences.getFloat("gpa", 0f).toDouble()
        val phone = sharedPreferences.getString("phone", null)
        val courseNumber = sharedPreferences.getInt("courseNumber", -1)
        val education = sharedPreferences.getString("education", null)
        val address = sharedPreferences.getString("address", null)
        val birthDate = sharedPreferences.getString("birthDate", null)

        val groupName = sharedPreferences.getString("groupName", null)
        val groupAverageGpa = sharedPreferences.getFloat("groupAverageGpa", 0f).toDouble()
        val groupStudentCount = sharedPreferences.getInt("groupStudentCount", -1)

        return if (login != null && password != null && role != null && firstName != null && lastName != null) {
            User(
                login, password, role, firstName, lastName, gpa, phone ?: "", courseNumber,
                education ?: "", address ?: "", birthDate ?: "", Group(groupName ?: "", groupStudentCount, groupAverageGpa)
            )
        } else {
            null
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}