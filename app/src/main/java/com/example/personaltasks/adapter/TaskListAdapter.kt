package com.example.personaltasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltasks.databinding.TaskDetailBinding
import com.example.personaltasks.model.Task
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TaskListAdapter(
    private val tasks: List<Task>
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: TaskDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            with(binding) {
                titleTv.text = task.title
                descriptionTv.text = task.description
                dueDateTv.text = task.dueDate.format(
                    DateTimeFormatter.ofLocalizedDate
                    (FormatStyle.MEDIUM))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TaskDetailBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}
