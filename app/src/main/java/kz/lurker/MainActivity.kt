package kz.lurker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
import kz.lurker.ui.GroupActivity
import kz.lurker.ui.LoginActivity
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
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        tokenService = TokenService(application)

        amUserName = findViewById(R.id.amUserName)
        amGroupName = findViewById(R.id.amGroupName)
        amGPA = findViewById(R.id.amGPA)
        amCourseNo = findViewById(R.id.amCourseNo)
        amGroupRating = findViewById(R.id.amGroupRating)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            tokenService.clearToken()
            sharedPreferences.edit().putBoolean("isLogged", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        val btnGrades: View = findViewById(R.id.buttonGrades)
        btnGrades.setOnClickListener {
            startActivity(Intent(this, GradesActivity::class.java))
        }

        getUserInfo(tokenService.getToken())

        val groupLayout = findViewById<LinearLayout>(R.id.groupLayout)
        groupLayout.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
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
                throw e
            }
        }
    }

    private fun displayUserInfo(user: User) {
        amUserName.text = "${user.firstName} ${user.lastName}"
        amGroupName.text = user.group.name
        amGPA.text= user.gpa.toString()
        amCourseNo.text = user.courseNumber.toString()
        amGroupRating.text = user.rating.toString()
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
        editor.putInt("rating", user.rating)
        editor.putString("groupName", user.group.name)
        editor.putString("groupAverageGpa", String.format("%.2f", user.group.averageGpa))
        editor.putInt("groupStudentCount", user.group.studentCount)
        editor.putLong("groupId", user.group.id)

        editor.apply()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
