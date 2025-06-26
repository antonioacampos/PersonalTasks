package com.example.personaltasks.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.personaltasks.model.FirebaseTaskService
import com.example.personaltasks.model.Task
import com.example.personaltasks.ui.Extras.EXTRA_TASK
import com.example.personaltasks.ui.Extras.EXTRA_VIEW_MODE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : AppCompatActivity(), OnTaskClickListener {

    private val taskController: TaskController by lazy {
        TaskController.getInstance(this)
    }

    private val firebaseService = FirebaseTaskService()

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val tasks = mutableListOf<Task>()
    private val taskAdapter: TaskRvAdapter by lazy {
        TaskRvAdapter(tasks, this)
    }

    private val createTaskArl = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data.getParcelableExtra(EXTRA_TASK, Task::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data.getParcelableExtra(EXTRA_TASK)
                }
                val position = data.getIntExtra("TASK_POSITION", -1)

                task?.let { receivedTask ->
                    lifecycleScope.launch {
                        val localId = withContext(Dispatchers.IO) {
                            taskController.createTask(receivedTask)
                        }
                        val taskWithLocalId = receivedTask.copy(id = localId.toInt())

                        firebaseService.saveTask(taskWithLocalId) { success, firebaseId ->
                            if (success && firebaseId != null) {
                                val updatedTask = taskWithLocalId.copy(firebaseId = firebaseId)

                                lifecycleScope.launch {
                                    taskController.updateTask(updatedTask)
                                    if (position >= 0) {
                                        updateTaskInList(updatedTask, position)
                                    } else {
                                        addTaskToList(updatedTask)
                                    }
                                }
                            } else {
                                Log.e("MainActivity", "Falha ao salvar no Firebase: $firebaseId")
                            }
                        }
                    }
                }

            }
            loadTasksFromDatabase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
            return
        }

        setupUi()
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSearchBar() {
        binding.searchBar.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchText = s?.toString()?.takeIf { it.isNotBlank() }
                performSearch()
            }
        })

        binding.searchBar.startDateEditText.setOnClickListener {
            showDatePicker { date ->
                currentStartDate = date
                binding.searchBar.startDateEditText.setText(formatDate(date))
                performSearch()
            }
        }

        binding.searchBar.endDateEditText.setOnClickListener {
            showDatePicker { date ->
                currentEndDate = date
                binding.searchBar.endDateEditText.setText(formatDate(date))
                performSearch()
            }
        }

        binding.searchBar.searchButton.setOnClickListener {
            performSearch()
        }

        binding.searchBar.clearFiltersButton.setOnClickListener {
            clearFilters()
        }
    }
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
            R.id.action_deleted_tasks -> {
                startActivity(Intent(this, DeletedTasksActivity::class.java))
                true
            }
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadTasksFromDatabase() {
        lifecycleScope.launch {
            val databaseTasks = withContext(Dispatchers.IO) { taskController.getAllTasks() }
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

    override fun onResume() {
        super.onResume()
        loadTasksFromDatabase()
    }


    override fun onRemoveTask(position: Int) {
        val taskToRemove = tasks[position]

        lifecycleScope.launch {
            val updatedTask = taskToRemove.copy(isDeleted = true)
            withContext(Dispatchers.IO) {
                taskController.updateTask(updatedTask)
            }
            tasks.removeAt(position)
            runOnUiThread {
                taskAdapter.notifyItemRemoved(position)
                Toast.makeText(
                    this@MainActivity,
                    "${taskToRemove.title} movida para excluídas!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!taskToRemove.firebaseId.isNullOrEmpty()) {
                firebaseService.deleteTask(taskToRemove.firebaseId) { success, error ->
                    if (!success) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Erro ao excluir no Firebase: ${error ?: "Desconhecido"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateTaskInList(task: Task, position: Int) {
        tasks[position] = task
        runOnUiThread {
            taskAdapter.notifyItemChanged(position)
            Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTaskToList(task: Task) {
        tasks.add(task)
        runOnUiThread {
            taskAdapter.notifyItemInserted(tasks.lastIndex)
            Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EDIT_TASK_REQUEST_CODE = 1
    }
}
