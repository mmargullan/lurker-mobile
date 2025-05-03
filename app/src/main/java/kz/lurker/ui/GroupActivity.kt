package kz.lurker.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kz.lurker.R

class GroupActivity: AppCompatActivity() {

    private var groupName: String? = ""
    private var groupGpa: String? = ""
    private var groupRating: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        setContentView(R.layout.activity_group)

        groupName = sharedPreferences.getString("groupName", "")
        groupGpa = sharedPreferences.getString("groupAverageGpa", "")
        groupRating = sharedPreferences.getInt("rating", 0).toLong()

        var etGroupName = findViewById<TextView>(R.id.etGroupName)
        var etGroupGpa = findViewById<TextView>(R.id.etGroupGpa)
        var etGroupRating = findViewById<TextView>(R.id.etGroupRating)

        etGroupName.text = groupName.toString()
        etGroupGpa.text = groupGpa.toString()
        etGroupRating.text = groupRating.toString()
    }

}