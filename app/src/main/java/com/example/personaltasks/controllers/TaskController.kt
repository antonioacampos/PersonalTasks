package com.example.personaltasks.controllers

import android.content.Context
import androidx.annotation.WorkerThread
import com.example.personaltasks.model.Task
import com.example.personaltasks.model.TaskDAO
import com.example.personaltasks.model.TaskRoomDatabase
import com.example.personaltasks.ui.MainActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskController(
    private val taskDao: TaskDAO,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        fun getInstance(mainActivity: MainActivity): TaskController {
            val context = mainActivity.applicationContext
            val database = TaskRoomDatabase.getDatabase(context)
            return TaskController(database.taskDao())
        }
    }

    @WorkerThread
    suspend fun createTask(task: Task): Long = withContext(dispatcher) {
        taskDao.insert(task)
    }

    @WorkerThread
    suspend fun getTask(id: Long): Task? = withContext(dispatcher) {
        taskDao.getById(id)
    }

    @WorkerThread
    suspend fun getAllTasks(): List<Task> = withContext(dispatcher) {
        taskDao.getAll()
    }

    @WorkerThread
    suspend fun updateTask(task: Task): Int = withContext(dispatcher) {
        taskDao.update(task)
    }

    @WorkerThread
    suspend fun removeTask(task: Task): Int = withContext(dispatcher) {
        taskDao.delete(task)
    }

    @WorkerThread
    suspend fun removeAllTasks() = withContext(dispatcher) {
        taskDao.deleteAll()
    }
}
