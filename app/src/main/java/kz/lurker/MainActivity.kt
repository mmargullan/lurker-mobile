package kz.lurker

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.serialization.json.Json
import kz.lurker.model.User
import kz.lurker.service.TokenService
import kz.lurker.ui.GradesActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tokenService: TokenService
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private lateinit var amUserName: TextView
    private lateinit var amGroupName: TextView
    private lateinit var amGroupRating: TextView
    private lateinit var amCourseNo: TextView
    private lateinit var amGPA: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tokenService = TokenService(application)

        amUserName    = findViewById(R.id.amUserName)
        amGroupName   = findViewById(R.id.amGroupName)
        amGPA         = findViewById(R.id.amGPA)
        amCourseNo    = findViewById(R.id.amCourseNo)
        amGroupRating = findViewById(R.id.amGroupRating)

        val btnGrades: View = findViewById(R.id.buttonGrades)
        btnGrades.setOnClickListener {
            startActivity(Intent(this, GradesActivity::class.java))
        }

        getUserInfo(tokenService.getToken())
    }

    private fun getUserInfo(token: String?) {
        lifecycleScope.launch {
            try {
                val response = client.get("https://test-student-forum.serveo.net/api/auth-api/user/getUser") {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
                if (response.status == HttpStatusCode.OK) {
                    val userInfo = response.body<User>()
                    saveUser(userInfo)
                    displayUserInfo(userInfo)
                } else {
                    throw Exception("Failed to get user data: ${response.status}")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Unknown error")
            }
        }
    }

    private fun getStudentRating(groupId: Long) {
        lifecycleScope.launch {
            try {
                val rating = client.get("https://test-student-forum.serveo.net/api/auth-api/group/getStudentRating/$groupId") {
                    headers.append(HttpHeaders.Authorization, "Bearer ${tokenService.getToken()}")
                }.body<Int>()
                amGroupRating.text = rating.toString()
            } catch (e: Exception) {
                showError(e.message ?: "Unknown error")
            }
        }
    }

    private fun displayUserInfo(user: User) {
        amUserName.text    = "${user.firstName} ${user.lastName}"
        amGroupName.text   = user.group.name
        amGPA.text         = user.gpa.toString()
        amCourseNo.text    = user.courseNumber.toString()
        getStudentRating(user.group.id)
    }

    private fun saveUser(user: User) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("login", user.login)
            putString("password", user.password)
            putString("role", user.role)
            putString("firstName", user.firstName)
            putString("lastName", user.lastName)
            putFloat("gpa", user.gpa.toFloat())
            putString("phone", user.phone)
            putInt("courseNumber", user.courseNumber)
            putString("education", user.education)
            putString("address", user.address)
            putString("birthDate", user.birthDate)
            putString("groupName", user.group.name)
            putFloat("groupAverageGpa", user.group.averageGpa.toFloat())
            putInt("groupStudentCount", user.group.studentCount)
            putLong("groupId", user.group.id)
            apply()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
