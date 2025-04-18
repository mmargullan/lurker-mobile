package kz.lurker.model

import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    val name: String = "",
    val mark: String = ""
)


@Serializable
data class Grade(
    val subjectName: String,
    val tutorList: String,
    val exams: List<Exam>
)