package kz.lurker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kz.lurker.R
import kz.lurker.model.Grade

class GradesAdapter(
    private var grades: List<Grade>
) : RecyclerView.Adapter<GradesAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val tvTutors: TextView = view.findViewById(R.id.tvTutors)
        val rvExams: RecyclerView = view.findViewById(R.id.rvExams)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val grade = grades[position]
        holder.tvSubject.text = grade.subjectName
        holder.tvTutors.text = "Преподаватели: ${grade.tutorList}"

        // Фильтрация экзаменов: убираем "Ср.тек. 11", "Ср.тек. 22" и пустые
        val filtered = grade.exams
            .filter { !it.name.startsWith("Ср.тек.") }
            .filter { !it.mark.isNullOrBlank() && it.mark != "-" }

        holder.rvExams.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvExams.adapter = ExamsAdapter(filtered)
    }

    override fun getItemCount(): Int = grades.size

    fun updateData(newGrades: List<Grade>) {
        grades = newGrades
        notifyDataSetChanged()
    }
}
