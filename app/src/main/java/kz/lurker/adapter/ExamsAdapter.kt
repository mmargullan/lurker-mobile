package kz.lurker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kz.lurker.R
import kz.lurker.model.Exam

class ExamsAdapter(
    private val exams: List<Exam>
) : RecyclerView.Adapter<ExamsAdapter.ExamViewHolder>() {

    inner class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExamName: TextView = view.findViewById(R.id.tvExamName)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvPercent: TextView = view.findViewById(R.id.tvPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = exams[position]
        holder.tvExamName.text = exam.name

        val percent = exam.mark
            ?.toFloatOrNull()
            ?.coerceIn(0f, 100f)
            ?: 0f

        holder.progressBar.progress = percent.toInt()
        holder.tvPercent.text = "${percent.toInt()}%"

        // Выбор цвета в зависимости от диапазона
        val colorRes = when {
            percent <= 25f -> R.color.progress_low
            percent <= 50f -> R.color.progress_mid_low
            percent <= 75f -> R.color.progress_mid_high
            else            -> R.color.progress_high
        }
        holder.progressBar.progressTintList =
            ContextCompat.getColorStateList(holder.itemView.context, colorRes)
    }


    override fun getItemCount(): Int = exams.size
}
