package com.example.personaltasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltasks.R
import com.example.personaltasks.adapter.TaskListAdapter
import com.example.personaltasks.controllers.TaskController
import com.example.personaltasks.databinding.ActivityMainBinding
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.ui.Extras.EXTRA_VIEW_MODE
import java.time.LocalDate

class MainActivity : AppCompatActivity(), OnTaskClickListener {

    private val taskController: TaskController by lazy {
        TaskController(this)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val createTaskArl = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val task = it.getParcelableExtra<Task>(EXTRA_TASK)
                val position = it.getIntExtra("TASK_POSITION", -1)
                if (task != null) {
                    if (position >= 0) {
                        tasks[position] = task
                        taskAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "Tarefa atualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        tasks.add(task)
                        taskAdapter.notifyItemInserted(tasks.lastIndex)
                        Toast.makeText(this, "Tarefa adicionada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private val tasks = mutableListOf<Task>()
    private val taskAdapter: TaskListAdapter by lazy {
        TaskListAdapter(tasks)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUi()
        loadSampleTasks()
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
            R.id.add_contact_mi -> {
                navigateToTaskCreation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToTaskCreation() {
        startActivity(Intent(this, TasksActivity::class.java))
    }

    private fun loadSampleTasks() {
        tasks.clear()
        (0..20).forEach { index ->
            tasks.add(
                Task(
                    title = "Task $index",
                    description = "Description $index",
                    dueDate = LocalDate.now().plusDays(index.toLong())
                )
            )
        }
        taskAdapter.notifyDataSetChanged()
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
            createTaskArl.launch(this)
        }
    }

    override fun onRemoveTask(position: Int) {
        val taskToRemove = tasks[position]
        taskController.removeTask(taskToRemove)
        tasks.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
    }
}
