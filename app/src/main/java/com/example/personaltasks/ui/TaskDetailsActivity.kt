package com.example.personaltasks.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.R
import com.example.personaltasks.model.FirebaseTaskService
import com.example.personaltasks.model.Task

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dueDateTextView: TextView
    private lateinit var firebaseService: FirebaseTaskService

    private var taskId: String? = null
    private var isDeleted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        supportActionBar?.title = "Detalhes da Tarefa"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        firebaseService = FirebaseTaskService()

        taskId = intent.getStringExtra("task_id")
        isDeleted = intent.getBooleanExtra("is_deleted", false)

        loadTaskDetails()
    }

    private fun initViews() {
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        dueDateTextView = findViewById(R.id.dueDateTextView)
    }

    private fun loadTaskDetails() {
        taskId?.let { id ->
            firebaseService.getTaskById(id) { task ->
                task?.let {
                    displayTaskDetails(it)
                } ?: run {
                    Toast.makeText(this, "Erro ao carregar detalhes da tarefa", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } ?: run {
            displayTaskFromIntent()
        }
    }

    private fun displayTaskDetails(task: Task) {
        titleTextView.text = task.title
        descriptionTextView.text = task.description
        dueDateTextView.text = "Prazo: ${task.dueDate}"
    }

    private fun displayTaskFromIntent() {
        val title = intent.getStringExtra("task_title") ?: ""
        val description = intent.getStringExtra("task_description") ?: ""
        val dueDate = intent.getStringExtra("task_due_date") ?: ""

        titleTextView.text = title
        descriptionTextView.text = description
        dueDateTextView.text = "Prazo: $dueDate"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
