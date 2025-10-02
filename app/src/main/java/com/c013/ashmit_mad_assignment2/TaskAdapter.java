package com.c013.ashmit_mad_assignment2;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskAdapter - RecyclerView Adapter for displaying tasks
 * Handles the display and interaction of task items in the RecyclerView
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    /**
     * Interface for handling task click events
     */
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    /**
     * Constructor
     * @param listener Click listener for task interactions
     */
    public TaskAdapter(OnTaskClickListener listener) {
        this.taskList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Bind task data to the view holder
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Update the task list and refresh the RecyclerView
     * @param tasks New list of tasks
     */
    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    /**
     * Get task at specific position
     * @param position Position in the list
     * @return Task object at that position
     */
    public Task getTaskAt(int position) {
        return taskList.get(position);
    }

    /**
     * ViewHolder class for task items
     * Holds references to views in each task item
     */
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDescription;
        private TextView tvPriority;
        private CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            cardView = itemView.findViewById(R.id.card_view);

            // Setup click listener for normal click (to edit task)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTaskClick(taskList.get(position));
                    }
                }
            });

            // Setup long click listener (alternative delete method)
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTaskLongClick(taskList.get(position));
                        return true;
                    }
                    return false;
                }
            });
        }

        /**
         * Bind task data to views
         * @param task Task object to display
         */
        public void bind(Task task) {
            // Set task description
            tvDescription.setText(task.getDescription());

            // Set priority text
            tvPriority.setText(task.getPriority());

            // Set color based on priority
            int color;
            switch (task.getPriority()) {
                case "High":
                    color = Color.parseColor("#FF5252"); // Red
                    break;
                case "Medium":
                    color = Color.parseColor("#FFA726"); // Orange
                    break;
                case "Low":
                    color = Color.parseColor("#66BB6A"); // Green
                    break;
                default:
                    color = Color.GRAY;
                    break;
            }

            // Apply color to priority text
            tvPriority.setTextColor(color);

            // Set card background to white
            cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }
}