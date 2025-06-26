package com.example.personaltasks.adapter

class DeletedTasksAdapter(
    private var tasks: MutableList<Task>,
    private val onItemLongClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<DeletedTasksAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val dueDateTextView: TextView = view.findViewById(R.id.dueDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deleted_task, parent, false)
        return ViewHolder(view)
    }
}
