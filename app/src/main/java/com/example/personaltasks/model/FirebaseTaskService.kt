package com.example.personaltasks.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseTaskService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun saveTask(task: Task, onComplete: (Boolean, String?) -> Unit) {
        val taskWithUserId = task.copy(userId = getCurrentUserId())

        if (task.firebaseId.isEmpty()) {
            db.collection("tasks")
                .add(taskWithUserId)
                .addOnSuccessListener { documentReference ->
                    onComplete(true, documentReference.id)
                }
                .addOnFailureListener { exception ->
                    onComplete(false, exception.message)
                }
        } else {
            db.collection("tasks")
                .document(task.firebaseId)
                .set(taskWithUserId)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    onComplete(false, exception.message)
                }
        }
    }

    fun getActiveTasks(onComplete: (List<Task>) -> Unit) {
        db.collection("tasks")
            .whereEqualTo("userId", getCurrentUserId())
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onComplete(emptyList())
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Task::class.java)?.copy(firebaseId = document.id)
                } ?: emptyList()

                onComplete(tasks)
            }
    }
}
