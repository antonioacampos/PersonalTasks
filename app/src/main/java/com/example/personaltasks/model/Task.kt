package com.example.personaltasks.model

import java.time.LocalDate
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Task(var title: String, var description: String, var dueDate: LocalDate) : Parcelable{

}