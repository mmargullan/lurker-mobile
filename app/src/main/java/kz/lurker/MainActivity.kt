package kz.lurker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import kz.lurker.service.NotificationService
import kz.lurker.service.TokenService
import kz.lurker.ui.GroupActivity
import kz.lurker.ui.GradesActivity
import kz.lurker.ui.UserActivity

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

        amUserName = findViewById(R.id.amUserName)
        amGroupName = findViewById(R.id.amGroupName)
        amGPA = findViewById(R.id.amGPA)
        amCourseNo = findViewById(R.id.amCourseNo)
        amGroupRating = findViewById(R.id.amGroupRating)

        findViewById<View>(R.id.buttonGrades).setOnClickListener {
            startActivity(Intent(this, GradesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.groupLayout).setOnClickListener {
            startActivity(Intent(this, GroupActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.buttonProfile).setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
        handleNotificationService()
        val token = tokenService.getToken()
        getUserInfo(token)
    }

    private fun handleNotificationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android O+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startNotificationService()
            }
        } else {
            startNotificationService()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startNotificationService()
        }
        // убрал else
    }

    private fun startNotificationService() {
        val intent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun getUserInfo(token: String?) {
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Unauthorized", Toast.LENGTH_SHORT).show()
            return
        }
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
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${response.status}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun displayUserInfo(user: User) {
        amUserName.text = "${user.firstName} ${user.lastName}"
        amGroupName.text = user.group.name
        amGPA.text = user.gpa.toString()
        amCourseNo.text = user.courseNumber.toString()
        amGroupRating.text = user.rating.toString()
    }

    private fun saveUser(user: User) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
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
            putInt("rating", user.rating)
            putString("groupName", user.group.name)
            putString("groupAverageGpa", String.format("%.2f", user.group.averageGpa))
            putInt("groupStudentCount", user.group.studentCount)
            putLong("groupId", user.group.id)
            apply()
        }
    }
}
