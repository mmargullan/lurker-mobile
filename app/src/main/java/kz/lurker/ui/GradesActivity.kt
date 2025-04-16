package kz.lurker.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kz.lurker.R
import kz.lurker.adapter.GradesAdapter
import kz.lurker.model.Grade
import kz.lurker.service.TokenService
import android.util.Log

class GradesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Grades"
    }

    private lateinit var adapter: GradesAdapter
    private var selectedYear = 2024
    private var selectedSemester = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grades)

        // Toolbar
        findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle("Grades")
            setTitleTextColor(resources.getColor(android.R.color.white))
            setBackgroundColor(resources.getColor(R.color.red_700))
            setSupportActionBar(this)
        }

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = GradesAdapter(emptyList())
        rv.adapter = adapter

        fetchGrades()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_grades, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_select_year -> {
            showPicker("Select Year", arrayOf("2022", "2023", "2024", "2025")) { year ->
                selectedYear = year.toInt()
                fetchGrades()
            }
            true
        }
        R.id.action_select_semester -> {
            showPicker("Select Semester", arrayOf("1", "2")) { sem ->
                selectedSemester = sem.toInt()
                fetchGrades()
            }
            true
        }
        R.id.action_home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showPicker(title: String, options: Array<String>, onSelect: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which -> onSelect(options[which]) }
            .show()
    }

    private fun fetchGrades() {
        Log.d(TAG, "fetchGrades() called: year=$selectedYear, semester=$selectedSemester")

        lifecycleScope.launch {
            try {
                val token = TokenService(this@GradesActivity).getToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "No token found, cannot fetch grades")
                    Toast.makeText(this@GradesActivity, "Неавторизован", Toast.LENGTH_LONG).show()
                    return@launch
                }
                Log.d(TAG, "Using token: ${token.take(10)}…")

                val client = HttpClient(OkHttp) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

                val url = "http://192.168.1.35:8081/grades/getGrades?year=$selectedYear&semester=$selectedSemester"
                Log.d(TAG, "Sending GET $url")

                val response: List<Grade> = client.get(url) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }.body()

                Log.d(TAG, "Received ${response.size} grades from server")

                // Фильтрация экзаменов по тем же правилам, что на фронте
                val processed = response.map { grade ->
                    val filteredExams = grade.exams
                        .filter { it.name.isNotBlank() && it.mark != null }
                        .filter { !it.name.startsWith("Ср.тек. 11") && !it.name.startsWith("Ср.тек. 22") }
                    Log.d(TAG, "Course '${grade.subjectName}': kept ${filteredExams.size} exams out of ${grade.exams.size}")
                    grade.copy(exams = filteredExams)
                }

                if (processed.isEmpty()) {
                    Log.w(TAG, "No grades after filtering for year=$selectedYear sem=$selectedSemester, switching semester")
                    val (prevYear, prevSem) = if (selectedSemester == 1) {
                        selectedYear - 1 to 2
                    } else {
                        selectedYear to 1
                    }
                    selectedYear = prevYear
                    selectedSemester = prevSem
                    Log.d(TAG, "Retrying fetchGrades() for year=$selectedYear sem=$selectedSemester")
                    fetchGrades()
                } else {
                    adapter.updateData(processed)
                    Log.d(TAG, "Adapter updated with ${processed.size} courses")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching grades", e)
                Toast.makeText(this@GradesActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
