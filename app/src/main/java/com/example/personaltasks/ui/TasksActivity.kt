package com.example.personaltasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.databinding.ActivityTaskBinding
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.ui.Extras.EXTRA_VIEW_MODE
import java.time.LocalDate

class TasksActivity : AppCompatActivity() {

    private val binding: ActivityTaskBinding by lazy {
        ActivityTaskBinding.inflate(layoutInflater)
    }

    private var isEditMode = false
    private var originalTask: Task? = null
    private var taskPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupToolbar()
        handleIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = "New task"
    }

    private fun handleIntent() {
        originalTask = retrieveTaskFromIntent()
        taskPosition = intent.getIntExtra("TASK_POSITION", -1)

        originalTask?.let {
            isEditMode = true
            showTaskDetails(it)
        } ?: run {
            setupNewTask()
        }
    }

    private fun retrieveTaskFromIntent(): Task? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TASK)
        }
    }

    private fun showTaskDetails(task: Task) {
        supportActionBar?.subtitle = if (isEditMode) "Updating task" else "Task details"

        with(binding) {
            titleEt.setText(task.title)
            descriptionEt.setText(task.description)
            dueDateDp.updateDate(task.dueDate.year, task.dueDate.monthValue - 1, task.dueDate.dayOfMonth)

            val isViewMode = intent.getBooleanExtra(EXTRA_VIEW_MODE, false)
            listOf(titleEt, descriptionEt, dueDateDp).forEach { it.isEnabled = !isViewMode }
            saveBt.visibility = if (isViewMode) View.GONE else View.VISIBLE
        }

        setupButtons()
    }

    private fun setupNewTask() {
        supportActionBar?.subtitle = "New task"
        binding.saveBt.visibility = View.VISIBLE
        setupButtons()
    }

    private fun setupButtons() {
        binding.saveBt.setOnClickListener {
            handleSaveAction()
        }

        binding.cancelBt.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun handleSaveAction() {
        val newTask = createTaskFromInputs()

        if (validateTask(newTask)) {
            returnResult(newTask)
        } else {
            showValidationError()
        }
    }

    private fun createTaskFromInputs(): Task {
        return originalTask?.copy(
            title = binding.titleEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            dueDate = LocalDate.of(
                binding.dueDateDp.year,
                binding.dueDateDp.month + 1,
                binding.dueDateDp.dayOfMonth
            )
        ) ?: Task(
            id = 0,
            title = binding.titleEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            dueDate = LocalDate.of(
                binding.dueDateDp.year,
                binding.dueDateDp.month + 1,
                binding.dueDateDp.dayOfMonth
            )
        )
    }

    private fun validateTask(task: Task): Boolean {
        return task.title.isNotBlank() && task.description.isNotBlank()
    }

    private fun showValidationError() {
    }

    private fun returnResult(task: Task) {
        Intent().apply {
            putExtra(EXTRA_TASK, task)
            if (isEditMode) {
                putExtra("TASK_POSITION", taskPosition)
            }
            setResult(RESULT_OK, this)
        }
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }
}
