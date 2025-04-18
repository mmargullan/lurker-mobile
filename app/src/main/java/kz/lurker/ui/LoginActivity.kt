package kz.lurker.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kz.lurker.MainActivity
import kz.lurker.R
import kz.lurker.viewmodel.LoginViewModel
import kz.lurker.viewmodel.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        setContentView(R.layout.activity_login)

        if (sharedPreferences.getBoolean("isLogged", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(username, password)
            }
        }

        viewModel.loginResult.observe(this, Observer { result ->
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            if (result == "Login successful") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

}
