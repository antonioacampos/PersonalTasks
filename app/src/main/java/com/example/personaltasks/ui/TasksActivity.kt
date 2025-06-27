package com.example.personaltasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.databinding.ActivityTaskBinding
import com.example.personaltasks.model.Priority
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.ui.Extras.EXTRA_VIEW_MODE
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

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
        val priorities = Priority.values()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.prioritySpinner.adapter = adapter
        setupToolbar()
        handleIntent()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = "Nova tarefa"
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
        supportActionBar?.subtitle = if (isEditMode) "Editando tarefa" else "Detalhes da tarefa"

        with(binding) {
            titleEt.setText(task.title)
            descriptionEt.setText(task.description)
            val date = Date.from(task.dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val cal = Calendar.getInstance().apply { time = date }
            dueDateDp.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            prioritySpinner.selectedItem
            val isViewMode = intent.getBooleanExtra(EXTRA_VIEW_MODE, false)
            listOf(titleEt, descriptionEt, dueDateDp).forEach { it.isEnabled = !isViewMode }
            saveBt.visibility = if (isViewMode) View.GONE else View.VISIBLE
        }
    }

    private fun setupNewTask() {
        supportActionBar?.subtitle = "Nova tarefa"
        binding.saveBt.visibility = View.VISIBLE
        val cal = Calendar.getInstance()
        binding.dueDateDp.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
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
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.dueDateDp.year)
            set(Calendar.MONTH, binding.dueDateDp.month)
            set(Calendar.DAY_OF_MONTH, binding.dueDateDp.dayOfMonth)
        }

        val dueDate = LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        return originalTask?.copy(
            title = binding.titleEt.text.toString(),
            description = binding.descriptionEt.text.toString(),
            dueDate = LocalDate.of(
                binding.dueDateDp.year,
                binding.dueDateDp.month + 1,
                binding.dueDateDp.dayOfMonth
            ),
            isCompleted = binding.completionCb.isChecked
        ) ?: Task(
            id = 0,
            title = binding.titleEt.text.toString(),
            //priority = binding.prioritySpinner.selectedItem(getSelectedItem())
            description = binding.descriptionEt.text.toString(),
            dueDate = dueDate,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            isCompleted = false
        )
    }

    private fun validateTask(task: Task): Boolean {
        return task.title.isNotBlank() && task.description.isNotBlank()
    }

    private fun showValidationError() {
        Toast.makeText(this, "Preencha título e descrição!", Toast.LENGTH_SHORT).show()
    }

    private fun returnResult(task: Task) {
        Intent().apply {
            putExtra(EXTRA_TASK, task)
            if (isEditMode) {
                putExtra("TASK_POSITION", taskPosition)
            }
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return true
    }
}
