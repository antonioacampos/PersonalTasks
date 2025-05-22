package com.example.personaltasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltasks.R
import com.example.personaltasks.databinding.TaskDetailBinding
import com.example.personaltasks.model.Task
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.example.personaltasks.ui.OnTaskClickListener

class TaskListAdapter(
    private val tasks: List<Task>,
    private val onTaskClickListener: OnTaskClickListener
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: TaskDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            with(binding) {
                titleTv.text = task.title
                descriptionTv.text = task.description
                dueDateTv.text = task.dueDate.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                )

                root.setOnClickListener {
                    onTaskClickListener.onViewTask(adapterPosition)
                }
            }
        }

        init {
            binding.root.setOnCreateContextMenuListener { menu, v, menuInfo ->
                (onTaskClickListener as AppCompatActivity).menuInflater.inflate(
                    R.menu.menu_task_options,
                    menu
                )

                menu.findItem(R.id.menu_view_task).setOnMenuItemClickListener {
                    onTaskClickListener.onViewTask(adapterPosition)
                    true
                }

                menu.findItem(R.id.menu_edit_task).setOnMenuItemClickListener {
                    onTaskClickListener.onEditTask(adapterPosition)
                    true
                }

                menu.findItem(R.id.menu_remove_task).setOnMenuItemClickListener {
                    onTaskClickListener.onRemoveTask(adapterPosition)
                    true
                }
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
