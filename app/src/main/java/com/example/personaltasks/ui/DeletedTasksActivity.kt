package com.example.personaltasks.ui
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltasks.adapter.DeletedTasksAdapter
import com.example.personaltasks.model.FirebaseTaskService
import com.example.personaltasks.model.Task
import android.content.Intent
import com.example.personaltasks.controllers.TaskController
import kotlinx.coroutines.launch

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
}
