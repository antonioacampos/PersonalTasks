package com.example.personaltasks.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseTaskService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun saveTask(task: Task, onComplete: (Boolean, String?) -> Unit) {
        val taskWithUserId = task.copy(userId = getCurrentUserId(), priority = task.priority)

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

    fun getDeletedTasks(onComplete: (List<Task>) -> Unit) {
        db.collection("tasks")
            .whereEqualTo("userId", getCurrentUserId())
            .whereEqualTo("isDeleted", true)
            .orderBy("deletedAt", Query.Direction.DESCENDING)
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

    fun deleteTask(firebaseId: String, onComplete: (Boolean, String?) -> Unit) {
        val updates = mapOf(
            "isDeleted" to true,
            "deletedAt" to System.currentTimeMillis()
        )

        db.collection("tasks")
            .document(firebaseId)
            .update(updates)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun reactivateTask(firebaseId: String, onComplete: (Boolean, String?) -> Unit) {
        val updates = mapOf(
            "isDeleted" to false,
            "deletedAt" to 0L
        )

        db.collection("tasks")
            .document(firebaseId)
            .update(updates)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun getTaskById(firebaseId: String, onComplete: (Task?) -> Unit) {
        db.collection("tasks")
            .document(firebaseId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val task = document.toObject(Task::class.java)?.copy(firebaseId = document.id)
                    onComplete(task)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun permanentlyDeleteTask(firebaseId: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("tasks")
            .document(firebaseId)
            .delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }
}
