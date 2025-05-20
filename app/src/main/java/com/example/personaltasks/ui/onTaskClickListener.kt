package com.example.personaltasks.ui

interface OnTaskClickListener {
    fun onViewTask(position: Int)
    fun onEditTask(position: Int)
    fun onRemoveTask(position: Int)
}
