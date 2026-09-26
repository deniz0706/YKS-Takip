package com.deniz0706.ykstakip.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Locale

class ExamHistoryAdapter(
    private var items: List<Exam>,
    private val onClick: (Exam) -> Unit
) : RecyclerView.Adapter<ExamHistoryAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val subtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val net: TextView = view.findViewById(R.id.tvNet)
        val breakdown: TextView = view.findViewById(R.id.tvBreakdown)
    }

    fun submit(newItems: List<Exam>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exam_card, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val exam = items[position]
        holder.title.text = exam.title
        val displayDate = try {
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(exam.date)!!)
        } catch (e: Exception) { exam.date }
        holder.subtitle.text = "${exam.type.label}  •  $displayDate"
        holder.net.text = "${Fmt.net(exam.totalNet)} net"
        holder.breakdown.text =
            "${exam.totalCorrect} doğru · ${exam.totalWrong} yanlış · ${exam.totalBlank} boş · ${exam.totalTimeMinutes} dk"
        holder.itemView.setOnClickListener { onClick(exam) }
    }
}
