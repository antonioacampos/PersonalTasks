package com.example.personaltasks.model

import java.time.LocalDate
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Parcelize @Entity
data class Task(@PrimaryKey(autoGenerate = true) var id: Int, var title: String, var description: String, var dueDate: LocalDate) : Parcelable{

}