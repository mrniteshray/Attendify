package xcom.niteshray.apps.attendify.Faculty
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import xcom.niteshray.apps.attendify.R

class SubjectStatsAdapter : RecyclerView.Adapter<SubjectStatsAdapter.StatsViewHolder>() {
    private var subjectStats = listOf<SubjectStat>()

    data class SubjectStat(
        val subject: String,
        val totalStudents: Int,
        val averageAttendance: Float,
        val totalSessions: Int
    )

    inner class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvAttendancePercent: TextView = itemView.findViewById(R.id.tvAttendancePercent)
        private val progressAttendance: ProgressBar = itemView.findViewById(R.id.progressAttendance)
        private val tvTotalSessions: TextView = itemView.findViewById(R.id.tvTotalSessions)

        fun bind(stat: SubjectStat) {
            tvSubjectName.text = stat.subject
            tvAttendancePercent.text = "${stat.averageAttendance.toInt()}%"
            progressAttendance.progress = stat.averageAttendance.toInt()
            tvTotalSessions.text = "${stat.totalSessions} sessions"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(subjectStats[position])
    }

    override fun getItemCount() = subjectStats.size

    fun submitList(stats: List<SubjectStat>) {
        subjectStats = stats
        notifyDataSetChanged()
    }
}