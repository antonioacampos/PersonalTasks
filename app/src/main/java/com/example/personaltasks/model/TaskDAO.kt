package com.example.personaltasks.model

import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Query("SELECT * FROM Task WHERE id = :id AND userId = :userId")
    suspend fun getById(id: Long, userId: String): Task?

    @Query("SELECT * FROM Task WHERE isDeleted = 0 AND userId = :userId ORDER BY dueDate ASC")
    suspend fun getAll(userId: String): List<Task>

    @Query("SELECT * FROM Task WHERE isDeleted = 1 AND userId = :userId")
    suspend fun getDeletedTasks(userId: String): List<Task>

    @Update
    suspend fun update(task: Task): Int

    @Delete
    suspend fun delete(task: Task): Int

    @Query("DELETE FROM Task WHERE userId = :userId")
    suspend fun deleteAll(userId: String)

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :searchText || '%' AND isDeleted = 0 AND userId = :userId ORDER BY title ASC")
    suspend fun searchTasksByTitle(searchText: String, userId: String): List<Task>

    @Query("SELECT * FROM Task WHERE date(dueDate) = date(:date) AND isDeleted = 0 AND userId = :userId ORDER BY dueDate ASC")
    suspend fun getTasksByDate(date: String, userId: String): List<Task>

    @Query("SELECT * FROM Task WHERE date(dueDate) BETWEEN date(:startDate) AND date(:endDate) AND isDeleted = 0 AND userId = :userId ORDER BY dueDate ASC")
    suspend fun getTasksByDateRange(startDate: String, endDate: String, userId: String): List<Task>

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :searchText || '%' AND date(dueDate) BETWEEN date(:startDate) AND date(:endDate) AND isDeleted = 0 AND userId = :userId ORDER BY dueDate ASC, title ASC")
    suspend fun searchTasksByTitleAndDateRange(searchText: String, startDate: String, endDate: String, userId: String): List<Task>

    @Query("""
        SELECT * FROM Task 
        WHERE (:searchText IS NULL OR title LIKE '%' || :searchText || '%')
        AND (:startDate IS NULL OR date(dueDate) >= date(:startDate))
        AND (:endDate IS NULL OR date(dueDate) <= date(:endDate))
        AND isDeleted = 0
        AND userId = :userId
        ORDER BY dueDate ASC, title ASC
    """)
    suspend fun searchTasks(searchText: String?, startDate: String?, endDate: String?, userId: String): List<Task>

    @Query("DELETE FROM Task WHERE id = :taskId AND userId = :userId")
    suspend fun deleteById(taskId: Int, userId: String)

    @Query("SELECT COUNT(*) FROM Task WHERE isDeleted = 0 AND isCompleted = 0 AND userId = :userId")
    suspend fun getActiveTasksCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE isDeleted = 0 AND isCompleted = 1 AND userId = :userId")
    suspend fun getCompletedTasksCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE isDeleted = 1 AND userId = :userId")
    suspend fun getDeletedTasksCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE userId = :userId")
    suspend fun getTotalTasksCount(userId: String): Int

    @Query("SELECT * FROM Task WHERE isDeleted = 0 AND isCompleted = 0 AND date(dueDate) < date(:today) AND userId = :userId ORDER BY dueDate ASC")
    suspend fun getOverdueTasks(today: String, userId: String): List<Task>

    @Query("SELECT COUNT(*) FROM Task WHERE isDeleted = 0 AND isCompleted = 0 AND date(dueDate) < date(:today) AND userId = :userId")
    suspend fun getOverdueTasksCount(today: String, userId: String): Int

    @Query("SELECT * FROM Task WHERE isDeleted = 0 AND isCompleted = 0 AND date(dueDate) = date(:today) AND userId = :userId ORDER BY dueDate ASC")
    suspend fun getTodayTasks(today: String, userId: String): List<Task>

    @Query("SELECT COUNT(*) FROM Task WHERE isDeleted = 0 AND isCompleted = 0 AND date(dueDate) = date(:today) AND userId = :userId")
    suspend fun getTodayTasksCount(today: String, userId: String): Int
}