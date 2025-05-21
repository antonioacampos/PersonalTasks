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
        TaskController()
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
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableExtra(EXTRA_TASK, Task::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    it.getParcelableExtra(EXTRA_TASK)
                }
                val position = it.getIntExtra("TASK_POSITION", -1)

                task?.let { receivedTask ->
                    if (position >= 0) {
                        tasks[position] = receivedTask
                        taskAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                    } else {
                        tasks.add(receivedTask)
                        taskAdapter.notifyItemInserted(tasks.lastIndex)
                        Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val tasks = mutableListOf<Task>()
    private val taskAdapter: TaskListAdapter by lazy {
        TaskListAdapter(tasks, this)
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
            R.id.add_task_mi -> { // Alterado para add_task_mi
                createTaskArl.launch(Intent(this, TasksActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
            putExtra("TASK_POSITION", position)
            createTaskArl.launch(this)
        }
    }

    override fun onRemoveTask(position: Int) {
        val taskToRemove = tasks[position]
        taskController.removeTask(taskToRemove)
        tasks.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        Toast.makeText(this, "Tarefa removida!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EDIT_TASK_REQUEST_CODE = 1
    }
}
