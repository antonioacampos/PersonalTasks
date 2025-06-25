package com.example.personaltasks.model

import java.time.LocalDate
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Parcelize @Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val firebaseId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String = "",
    val deletedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()) : Parcelable {
}