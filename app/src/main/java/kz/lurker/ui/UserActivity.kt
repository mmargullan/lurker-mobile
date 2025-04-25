package kz.lurker.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kz.lurker.R

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val firstName = sharedPrefs.getString("firstName", "N/A")
        val lastName = sharedPrefs.getString("lastName", "N/A")
        val role = sharedPrefs.getString("role", "N/A")
        val gpa = sharedPrefs.getFloat("gpa", 0f)
        val phone = sharedPrefs.getString("phone", "N/A")
        val courseNumber = sharedPrefs.getInt("courseNumber", 0)
        val education = sharedPrefs.getString("education", "N/A")
        val address = sharedPrefs.getString("address", "N/A")
        val birthDate = sharedPrefs.getString("birthDate", "N/A")
        val groupName = sharedPrefs.getString("groupName", "N/A")
        val groupAvg = sharedPrefs.getString("groupAverageGpa", "N/A")
        val groupCount = sharedPrefs.getInt("groupStudentCount", 0)

        findViewById<TextView>(R.id.tvFullName).text = "$firstName $lastName"
        findViewById<TextView>(R.id.tvRole).text = "Role: $role"
        findViewById<TextView>(R.id.tvPhone).text = "Phone: $phone"
        findViewById<TextView>(R.id.tvCourse).text = "Course: $courseNumber"
        findViewById<TextView>(R.id.tvEducation).text = "Education: $education"
        findViewById<TextView>(R.id.tvAddress).text = "Address: $address"
        findViewById<TextView>(R.id.tvBirthDate).text = "Birth Date: $birthDate"
    }
}