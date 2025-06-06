package com.example.personaltasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltasks.R
import com.example.personaltasks.adapter.TaskRvAdapter
import com.example.personaltasks.controllers.TaskController
import com.example.personaltasks.databinding.ActivityMainBinding
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.ui.Extras.EXTRA_VIEW_MODE
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : AppCompatActivity(), OnTaskClickListener {

    private val taskController: TaskController by lazy {
        TaskController.getInstance(this)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val createTaskArl = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                lifecycleScope.launch {
                    val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data.getParcelableExtra(EXTRA_TASK, Task::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        data.getParcelableExtra(EXTRA_TASK)
                    }
                    val position = data.getIntExtra("TASK_POSITION", -1)

                    task?.let { receivedTask ->
                        if (position >= 0) {
                            taskController.updateTask(receivedTask)
                            updateTaskInList(receivedTask, position)
                        } else {
                            taskController.createTask(receivedTask)
                            addTaskToList(receivedTask)
                        }
                        loadTasksFromDatabase()
                    }
                }
            }
        }
    }

    private val tasks = mutableListOf<Task>()
    private val taskAdapter: TaskRvAdapter by lazy {
        TaskRvAdapter(tasks, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUi()
        loadTasksFromDatabase()
    }

    private fun setupUi() {
        setSupportActionBar(binding.toolbar.toolbar)
        configureTaskList()
    }

    private fun configureTaskList() {
        with(binding.taskList) {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_task_mi -> {
                createTaskArl.launch(Intent(this, TasksActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadTasksFromDatabase() {
        lifecycleScope.launch {
            val databaseTasks = taskController.getAllTasks()
            tasks.clear()
            tasks.addAll(databaseTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }

    override fun onViewTask(position: Int) {
        Intent(this, TasksActivity::class.java).apply {
            putExtra(EXTRA_TASK, tasks[position])
            putExtra(EXTRA_VIEW_MODE, true)
            startActivity(this)
        }
    }

    override fun onEditTask(position: Int) {
        Intent(this, TasksActivity::class.java).apply {
            putExtra(EXTRA_TASK, tasks[position])
            putExtra("TASK_POSITION", position)
            createTaskArl.launch(this)
        }
    }

    override fun onRemoveTask(position: Int) {
        lifecycleScope.launch {
            val taskToRemove = tasks[position]
            taskController.removeTask(taskToRemove)
            tasks.removeAt(position)
            runOnUiThread {
                taskAdapter.notifyItemRemoved(position)
                Toast.makeText(
                    this@MainActivity,
                    "${taskToRemove.title} removed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateTaskInList(task: Task, position: Int) {
        tasks[position] = task
        runOnUiThread {
            taskAdapter.notifyItemChanged(position)
            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTaskToList(task: Task) {
        tasks.add(task)
        runOnUiThread {
            taskAdapter.notifyItemInserted(tasks.lastIndex)
            Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EDIT_TASK_REQUEST_CODE = 1
    }
}
