package com.example.personaltasks.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltasks.R
import com.example.personaltasks.adapter.DeletedTasksAdapter
import com.example.personaltasks.model.FirebaseTaskService
import com.example.personaltasks.model.Task
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.personaltasks.controllers.TaskController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletedTasksActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeletedTasksAdapter
    private lateinit var firebaseService: FirebaseTaskService
    private var selectedTask: Task? = null
    private var selectedPosition: Int = -1

    private val taskController: TaskController by lazy {
        TaskController.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deleted_tasks)
        supportActionBar?.title = "Tarefas Excluídas"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseService = FirebaseTaskService()
        setupRecyclerView()
        loadDeletedTasks()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDeletedTasks)
        adapter = DeletedTasksAdapter(mutableListOf()) { task, position ->
            selectedTask = task
            selectedPosition = position
            registerForContextMenu(recyclerView)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDeletedTasks() {
        lifecycleScope.launch {
            val localDeletedTasks = withContext(Dispatchers.IO) {
                taskController.getDeletedTasks()
            }

            if (localDeletedTasks.isNotEmpty()) {
                adapter.updateTasks(localDeletedTasks)
            } else {
                firebaseService.getDeletedTasks { firebaseTasks ->
                    runOnUiThread {
                        adapter.updateTasks(firebaseTasks)
                    }
                }
            }
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        selectedTask?.let { task ->
            when (item.itemId) {
                1 -> {
                    reactivateTask(task)
                    return true
                }
                2 -> {
                    showTaskDetails(task)
                    return true
                }
                else -> {
                    return super.onContextItemSelected(item)
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun reactivateTask(task: Task) {
        if (task.firebaseId.isNullOrEmpty()) {
            lifecycleScope.launch {
                val reactivatedTask = task.copy(isDeleted = false)
                taskController.updateTask(reactivatedTask)
                loadDeletedTasks()
                Toast.makeText(
                    this@DeletedTasksActivity,
                    "Tarefa reativada apenas localmente (sem sincronização)",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        firebaseService.reactivateTask(task.firebaseId) { success, error ->
            if (success) {
                lifecycleScope.launch {
                    val reactivatedTask = task.copy(isDeleted = false)
                    taskController.updateTask(reactivatedTask)
                    loadDeletedTasks()
                    Toast.makeText(
                        this@DeletedTasksActivity,
                        "Tarefa reativada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@DeletedTasksActivity,
                    "Erro ao reativar: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTaskDetails(task: Task) {
        val intent = Intent(this, TaskDetailsActivity::class.java)
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_title", task.title)
        intent.putExtra("task_description", task.description)
        intent.putExtra("task_due_date", task.dueDate)
        intent.putExtra("is_deleted", true)
        startActivity(intent)
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
