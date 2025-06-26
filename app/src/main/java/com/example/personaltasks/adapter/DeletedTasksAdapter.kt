package com.example.personaltasks.adapter

class DeletedTasksAdapter(
    private var tasks: MutableList<Task>,
    private val onItemLongClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<DeletedTasksAdapter.ViewHolder>() {

}
