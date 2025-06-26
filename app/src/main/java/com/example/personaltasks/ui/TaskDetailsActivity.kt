package com.example.personaltasks.ui

import android.widget.TextView
import android.widget.Toast
import com.example.personaltasks.model.FirebaseTaskService
import com.example.personaltasks.model.Task

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dueDateTextView: TextView
    private lateinit var firebaseService: FirebaseTaskService

    private var taskId: String? = null
    private var isDeleted: Boolean = false
}
