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

    @Query("SELECT * FROM Task WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM Task WHERE isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getAll(): List<Task>

    @Query("SELECT * FROM Task WHERE isDeleted = 1")
    suspend fun getDeletedTasks(): List<Task>

    @Update
    suspend fun update(task: Task): Int

    @Delete
    suspend fun delete(task: Task): Int

    @Query("DELETE FROM Task")
    suspend fun deleteAll()

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :searchText || '%' AND isDeleted = 0 ORDER BY title ASC")
    suspend fun searchTasksByTitle(searchText: String): List<Task>

    @Query("SELECT * FROM Task WHERE date(dueDate) = date(:date) AND isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getTasksByDate(date: String): List<Task>

    @Query("SELECT * FROM Task WHERE date(dueDate) BETWEEN date(:startDate) AND date(:endDate) AND isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getTasksByDateRange(startDate: String, endDate: String): List<Task>

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :searchText || '%' AND date(dueDate) BETWEEN date(:startDate) AND date(:endDate) AND isDeleted = 0 ORDER BY dueDate ASC, title ASC")
    suspend fun searchTasksByTitleAndDateRange(searchText: String, startDate: String, endDate: String): List<Task>

    @Query("""
        SELECT * FROM Task 
        WHERE (:searchText IS NULL OR title LIKE '%' || :searchText || '%')
        AND (:startDate IS NULL OR date(dueDate) >= date(:startDate))
        AND (:endDate IS NULL OR date(dueDate) <= date(:endDate))
        AND isDeleted = 0
        ORDER BY dueDate ASC, title ASC
    """)
    suspend fun searchTasks(searchText: String?, startDate: String?, endDate: String?): List<Task>
}