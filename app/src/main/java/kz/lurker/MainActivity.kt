package kz.lurker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kz.lurker.service.LoginRequest
import kz.lurker.service.RetrofitInstance

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.login(LoginRequest(username, password))
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error logging in: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Internal server error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
