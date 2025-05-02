package kz.lurker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kz.lurker.model.NotificationMessage

class NotificationService : Service() {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    companion object {
        const val CHANNEL_ID = "notification_channel"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notification Service Running")
            .setContentText("This service is running in the background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()
        startForeground(1, notification)
        createNotificationChannel()
        startCheckingNotifications()
    }


    private fun startCheckingNotifications() {
        serviceScope.launch {
            while (true) {
                val userId = getUserIdFromPreferences()
                if (userId != null) {
                    checkNotifications(userId)
                }
                delay(5000)
            }
        }
    }

    private suspend fun checkNotifications(userId: String) {
        try {
            val response = client.get("https://test-student-forum.serveo.net/api/chat-api/notification/getByUserId?userId=$userId")
            Log.d("NotificationService", "Ответ сервера: ${response.body<String>()}")
            if (response.status == HttpStatusCode.OK) {
                val notifications = response.body<List<NotificationMessage>>()
                handleNotifications(notifications)
            } else {
                Log.e("NotificationService", "Ошибка при получении уведомлений: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Ошибка при запросе: $e")
        }
    }


    private fun handleNotifications(notifications: List<NotificationMessage>) {
        notifications.forEach {
            if (!it.text.isNullOrEmpty()) {
                showNotification(it.text)
            }
        }
    }

    private fun showNotification(text: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Новое уведомление")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(0, notification)
    }

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("login", null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        client.close()
    }
}