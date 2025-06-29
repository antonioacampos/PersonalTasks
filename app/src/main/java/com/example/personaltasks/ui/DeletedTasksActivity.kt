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
import androidx.appcompat.app.AlertDialog

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
                Log.d("DeletedTasks", "Carregadas ${localDeletedTasks.size} tarefas do banco local")
            } else {
                firebaseService.getDeletedTasks { firebaseTasks ->
                    runOnUiThread {
                        adapter.updateTasks(firebaseTasks)
                        Log.d("DeletedTasks", "Carregadas ${firebaseTasks.size} tarefas do Firebase")
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
                    showDeleteConfirmationDialog(task)
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
        Log.d("DeletedTasks", "Tentando reativar tarefa: ${task.title}, firebaseId: '${task.firebaseId}'")

        if (task.firebaseId.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val reactivatedTask = task.copy(isDeleted = false)
                    withContext(Dispatchers.IO) {
                        taskController.updateTask(reactivatedTask)
                    }
                    loadDeletedTasks()
                    setResult(RESULT_OK)
                    Toast.makeText(
                        this@DeletedTasksActivity,
                        "Tarefa reativada apenas localmente (sem sincronização)",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@DeletedTasksActivity,
                        "Erro ao reativar tarefa: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return
        }

        firebaseService.reactivateTask(task.firebaseId) { success, error ->
            if (success) {
                lifecycleScope.launch {
                    try {
                        val reactivatedTask = task.copy(isDeleted = false)
                        withContext(Dispatchers.IO) {
                            taskController.updateTask(reactivatedTask)
                        }
                        loadDeletedTasks()
                        setResult(RESULT_OK)
                        Toast.makeText(
                            this@DeletedTasksActivity,
                            "Tarefa reativada com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DeletedTasksActivity,
                            "Erro ao atualizar tarefa localmente: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@DeletedTasksActivity,
                    "Erro ao reativar no Firebase: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Definitivamente")
            .setMessage("Tem certeza que deseja excluir permanentemente a tarefa \"${task.title}\"?\n\nEsta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                deleteTaskPermanently(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteTaskPermanently(task: Task) {
        if (!task.firebaseId.isNullOrEmpty()) {
            firebaseService.permanentlyDeleteTask(task.firebaseId) { success, error ->
                if (success) {
                    deleteTaskFromLocal(task)
                } else {
                    showLocalDeleteDialog(task, error)
                }
            }
        } else {
            deleteTaskFromLocal(task)
        }
    }

    private fun deleteTaskFromLocal(task: Task) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    taskController.permanentlyDeleteTask(task.id)
                }
                loadDeletedTasks()
                Toast.makeText(
                    this@DeletedTasksActivity,
                    "Tarefa \"${task.title}\" excluída definitivamente",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@DeletedTasksActivity,
                    "Erro ao excluir tarefa: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLocalDeleteDialog(task: Task, error: String?) {
        AlertDialog.Builder(this)
            .setTitle("Erro no Firebase")
            .setMessage("Não foi possível excluir do Firebase: $error\n\nDeseja excluir apenas localmente?")
            .setPositiveButton("Sim") { _, _ ->
                deleteTaskFromLocal(task)
            }
            .setNegativeButton("Não", null)
            .show()
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

    override fun onDestroy() {
        super.onDestroy()
        selectedTask = null
    }
}
