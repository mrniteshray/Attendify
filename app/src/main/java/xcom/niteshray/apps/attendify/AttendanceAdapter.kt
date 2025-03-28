package xcom.niteshray.apps.attendify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xcom.niteshray.apps.attendify.Model.AttendanceSession

// Adapter Class
class AttendanceAdapter(
    private val sessions: List<AttendanceSession>,
    private val onMarkAttendanceClick: (String) -> Unit  // Callback for button click
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    // ViewHolder Class
    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectTextView: TextView = view.findViewById(R.id.tvSubject)
        val markAttendanceButton: Button = view.findViewById(R.id.btnMarkAttendance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val session = sessions[position]

        holder.subjectTextView.text = session.subject
        holder.markAttendanceButton.setOnClickListener {
            onMarkAttendanceClick(session.sessionId)  // Send sessionId when button is clicked
        }
    }

    override fun getItemCount(): Int = sessions.size
}