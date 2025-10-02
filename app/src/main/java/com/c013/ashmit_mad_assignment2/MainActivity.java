package com.c013.ashmit_mad_assignment2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAdd;
    private DatabaseReference databaseReference;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String databaseUrl = "https://c013todolist-default-rtdb.asia-southeast1.firebaseio.app";

        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
        recyclerView = findViewById(R.id.recycler_view);
        fabAdd = findViewById(R.id.fab_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this);
        recyclerView.setAdapter(taskAdapter);
        setupSwipeToDelete();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
        loadTasks();
    }
    private void loadTasks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList = new ArrayList<>();

                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        String firebaseKey = taskSnapshot.getKey();
                        task.setFirebaseKey(firebaseKey);
                        taskList.add(task);
                    }
                }

                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task t1, Task t2) {
                        return Integer.compare(t2.getPriorityValue(), t1.getPriorityValue());
                    }
                });
                taskAdapter.setTasks(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        "Failed to load tasks: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        final EditText etDescription = dialogView.findViewById(R.id.et_description);
        final Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);

        // Setup spinner with priority options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        builder.setView(dialogView)
                .setTitle("Add New Task")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString().trim();
                String priority = spinnerPriority.getSelectedItem().toString();

                if (!description.isEmpty()) {
                    addTask(description, priority);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Please enter task description",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTask(String description, String priority) {
        String taskId = databaseReference.push().getKey();
        if (taskId != null) {
            Task newTask = new Task(description, priority);
            databaseReference.child(taskId).setValue(newTask)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(MainActivity.this,
                                    "Task added successfully",
                                    Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this,
                                    "Failed to add task: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }
    }
    @Override
    public void onTaskClick(Task task) {
        showUpdateTaskDialog(task);
    }

    @Override
    public void onTaskLongClick(Task task) {
        showDeleteConfirmationDialog(task);
    }

    private void showUpdateTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        final EditText etDescription = dialogView.findViewById(R.id.et_description);
        final Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);

        etDescription.setText(task.getDescription());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(task.getPriority());
        spinnerPriority.setSelection(spinnerPosition);

        builder.setView(dialogView)
                .setTitle("Update Task")
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString().trim();
                String priority = spinnerPriority.getSelectedItem().toString();

                if (!description.isEmpty()) {
                    updateTask(task.getFirebaseKey(), description, priority);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Please enter task description",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTask(String firebaseKey, String description, String priority) {
        DatabaseReference taskRef = databaseReference.child(firebaseKey);
        taskRef.child("description").setValue(description);
        taskRef.child("priority").setValue(priority)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(MainActivity.this,
                                "Task updated successfully",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this,
                                "Failed to update task: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task.getFirebaseKey()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask(String firebaseKey) {
        DatabaseReference taskRef = databaseReference.child(firebaseKey);

        taskRef.removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(MainActivity.this,
                                "Task deleted successfully",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this,
                                "Failed to delete task: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = taskAdapter.getTaskAt(position);
                deleteTask(task.getFirebaseKey());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}