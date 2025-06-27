package com.example.personaltasks.controllers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.personaltasks.model.Task
import com.example.personaltasks.model.TaskDAO
import com.example.personaltasks.model.TaskRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TaskController private constructor(private val taskDao: TaskDAO) {
    private val dispatcher = Dispatchers.IO

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    suspend fun createTask(task: Task): Long = withContext(dispatcher) {
        val taskWithUserId = task.copy(userId = getCurrentUserId())
        taskDao.insert(taskWithUserId)
    }

    suspend fun getAllTasks(): List<Task> = withContext(dispatcher) {
        taskDao.getAll(getCurrentUserId())
    }

    suspend fun getDeletedTasks(): List<Task> = withContext(dispatcher) {
        taskDao.getDeletedTasks(getCurrentUserId())
    }

    suspend fun updateTask(task: Task): Int = withContext(dispatcher) {
        val taskWithUserId = task.copy(userId = getCurrentUserId())
        taskDao.update(taskWithUserId)
    }

    suspend fun removeTask(task: Task): Int = withContext(dispatcher) {
        taskDao.delete(task)
    }

    suspend fun searchTasksByTitle(searchText: String): List<Task> = withContext(dispatcher) {
        taskDao.searchTasksByTitle(searchText, getCurrentUserId())
    }

    suspend fun getTasksByDate(date: LocalDate): List<Task> = withContext(dispatcher) {
        taskDao.getTasksByDate(date.toString(), getCurrentUserId())
    }

    suspend fun getTasksByDateRange(startDate: LocalDate, endDate: LocalDate): List<Task> = withContext(dispatcher) {
        taskDao.getTasksByDateRange(startDate.toString(), endDate.toString(), getCurrentUserId())
    }

    suspend fun searchTasks(searchText: String?, startDate: LocalDate?, endDate: LocalDate?): List<Task> = withContext(dispatcher) {
        taskDao.searchTasks(
            searchText?.takeIf { it.isNotBlank() },
            startDate?.toString(),
            endDate?.toString(),
            getCurrentUserId()
        )
    }

    suspend fun permanentlyDeleteTask(taskId: Int) = withContext(dispatcher) {
        taskDao.deleteById(taskId, getCurrentUserId())
    }

    companion object {
        @Volatile private var instance: TaskController? = null

        fun getInstance(context: Context): TaskController {
            return instance ?: synchronized(this) {
                val database = TaskRoomDatabase.getDatabase(context.applicationContext)
                val dao = database.taskDao()
                val newInstance = TaskController(dao)
                instance = newInstance
                newInstance
            }
        }
    }

    suspend fun getActiveTasksCount(): Int = withContext(dispatcher) {
        taskDao.getActiveTasksCount(getCurrentUserId())
    }

    suspend fun getCompletedTasksCount(): Int = withContext(dispatcher) {
        taskDao.getCompletedTasksCount(getCurrentUserId())
    }

    suspend fun getDeletedTasksCount(): Int = withContext(dispatcher) {
        taskDao.getDeletedTasksCount(getCurrentUserId())
    }

    suspend fun getTotalTasksCount(): Int = withContext(dispatcher) {
        taskDao.getTotalTasksCount(getCurrentUserId())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOverdueTasks(): List<Task> = withContext(dispatcher) {
        val today = LocalDate.now().toString()
        taskDao.getOverdueTasks(today, getCurrentUserId())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOverdueTasksCount(): Int = withContext(dispatcher) {
        val today = LocalDate.now().toString()
        taskDao.getOverdueTasksCount(today, getCurrentUserId())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayTasks(): List<Task> = withContext(dispatcher) {
        val today = LocalDate.now().toString()
        taskDao.getTodayTasks(today, getCurrentUserId())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayTasksCount(): Int = withContext(dispatcher) {
        val today = LocalDate.now().toString()
        taskDao.getTodayTasksCount(today, getCurrentUserId())
    }
}

