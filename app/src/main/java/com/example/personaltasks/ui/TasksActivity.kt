package com.example.personaltasks.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.databinding.ActivityTaskBinding
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.model.Task
import java.time.LocalDate

class TasksActivity : AppCompatActivity() {

    private val binding: ActivityTaskBinding by lazy {
        ActivityTaskBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.subtitle = "Nova tarefa"

        val task = retrieveTaskFromIntent()

        if (task != null) {
            showTaskDetails(task)
        }
    }

    private fun retrieveTaskFromIntent(): Task? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Task>(EXTRA_TASK)
        }
    }

    private fun showTaskDetails(task: Task) {
        supportActionBar?.subtitle = "Detalhes da tarefa"
        with(binding) {
            titleEt.setText(task.title)
            descriptionEt.setText(task.description)
            dueDateDp.updateDate(task.dueDate.year, task.dueDate.monthValue - 1, task.dueDate.dayOfMonth)

            listOf(titleEt, descriptionEt, dueDateDp).forEach { it.isEnabled = false }
            saveBt.visibility = View.GONE
        }

        with(binding){
            saveBt.setOnClickListener {
                Task(
                    hashCode(),
                    titleEt.text.toString(),
                    descriptionEt.text.toString(),
                    LocalDate.of(dueDateDp.year, dueDateDp.month, dueDateDp.dayOfMonth)
                ).let { contact ->
                    Intent().apply{
                        putExtra(EXTRA_TASK, contact)
                        setResult(RESULT_OK, this)
                    }
                }
                finish()
            }
        }
        binding.cancelBt.setOnClickListener {
            finish()
        }
    }
}
