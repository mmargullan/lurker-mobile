package kz.lurker.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(
    val from: String?,
    val text: String?,
    val date: String?,
    val userId: String
)
