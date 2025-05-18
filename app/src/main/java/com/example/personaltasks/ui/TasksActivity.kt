package com.example.personaltasks.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.databinding.ActivityTasksBinding

class TasksActivity : AppCompatActivity() {
    private val atb: ActivityTasksBinding by lazy {
        ActivityTasksBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(atb.root)

        setSupportActionBar(atb.toolbar.toolbar)
    }
}