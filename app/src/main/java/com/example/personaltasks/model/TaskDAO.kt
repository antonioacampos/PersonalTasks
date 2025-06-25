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
}