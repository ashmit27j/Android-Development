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

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.taskList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return taskList.get(position);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDescription;
        private TextView tvPriority;
        private CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            cardView = itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTaskClick(taskList.get(position));
                    }
                }
            });

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

        public void bind(Task task) {
            tvDescription.setText(task.getDescription());
            tvPriority.setText(task.getPriority());
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
            tvPriority.setTextColor(color);
            cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }
}