package kz.lurker.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val login: String,
    val password: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val gpa: Double,
    val phone: String,
    val courseNumber: Int,
    val education: String,
    val address: String,
    val birthDate: String,
    val group: Group
)

@Serializable
data class Group(
        val name: String,
        val studentCount: Int,
        val averageGpa: Double
)