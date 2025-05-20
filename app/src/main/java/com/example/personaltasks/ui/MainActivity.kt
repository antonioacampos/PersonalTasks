package com.example.personaltasks.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltasks.R
import com.example.personaltasks.adapter.TaskListAdapter
import com.example.personaltasks.databinding.ActivityMainBinding
import com.example.personaltasks.model.Task
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
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
}
