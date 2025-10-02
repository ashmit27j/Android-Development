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

        // Initialize Firebase Database Reference
//        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
        // Initialize Firebase Database Reference with the FULL REGIONAL URL
        String databaseUrl = "https://c013todolist-default-rtdb.asia-southeast1.firebaseio.app";

        // Revert to simple getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        fabAdd = findViewById(R.id.fab_add);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this);
        recyclerView.setAdapter(taskAdapter);

        // Setup swipe to delete functionality
        setupSwipeToDelete();

        // Setup FAB click listener for adding new tasks
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        // Load tasks from Firebase
        loadTasks();
    }

    /**
     * Load all tasks from Firebase Realtime Database
     * Tasks are automatically sorted by priority (High -> Medium -> Low)
     */
    private void loadTasks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList = new ArrayList<>();

                // Iterate through all tasks in database
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        // Get the unique Firebase key from the snapshot.
                        String firebaseKey = taskSnapshot.getKey();
                        task.setFirebaseKey(firebaseKey); // Set the key on the Task object

                        taskList.add(task); // Add the task to the list
                    }
                }

                // Sort tasks by priority: High (3) -> Medium (2) -> Low (1)
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task t1, Task t2) {
                        // Sort in descending order (higher priority first)
                        return Integer.compare(t2.getPriorityValue(), t1.getPriorityValue());
                    }
                });

                // Update RecyclerView with sorted tasks
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

    /**
     * Show dialog to add a new task
     */
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
                .setPositiveButton("Add", null)  // Set to null initially
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to prevent auto-dismiss
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

    /**
     * Add a new task to Firebase
     * @param description Task description
     * @param priority Task priority (High, Medium, Low)
     */
    private void addTask(String description, String priority) {
        // Generate unique task ID
        String taskId = databaseReference.push().getKey();

        if (taskId != null) {
            // 💡 MODIFICATION: Create new Task object using the constructor WITHOUT taskId
            Task newTask = new Task(description, priority);

            // Save to Firebase (taskId is the path key)
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

    /**
     * Handle task click - Show update dialog
     */
    @Override
    public void onTaskClick(Task task) {
        showUpdateTaskDialog(task);
    }

    /**
     * Handle task long click - Show delete confirmation
     */
    @Override
    public void onTaskLongClick(Task task) {
        showDeleteConfirmationDialog(task);
    }

    /**
     * Show dialog to update an existing task
     */
    private void showUpdateTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        final EditText etDescription = dialogView.findViewById(R.id.et_description);
        final Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);

        // Pre-fill current task values
        etDescription.setText(task.getDescription());

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        // Set current priority in spinner
        int spinnerPosition = adapter.getPosition(task.getPriority());
        spinnerPriority.setSelection(spinnerPosition);

        builder.setView(dialogView)
                .setTitle("Update Task")
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString().trim();
                String priority = spinnerPriority.getSelectedItem().toString();

                if (!description.isEmpty()) {
                    // 💡 MODIFICATION: Use getFirebaseKey()
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

    /**
     * Update task in Firebase
     * @param firebaseKey Task ID
     * @param description New description
     * @param priority New priority
     */
    private void updateTask(String firebaseKey, String description, String priority) {
        // 💡 MODIFICATION: Use firebaseKey
        DatabaseReference taskRef = databaseReference.child(firebaseKey);

        // Update description
        taskRef.child("description").setValue(description);

        // Update priority
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

    /**
     * Show confirmation dialog before deleting task
     */
    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                // 💡 MODIFICATION: Use getFirebaseKey()
                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task.getFirebaseKey()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete task from Firebase
     * @param firebaseKey Task ID to delete
     */
    private void deleteTask(String firebaseKey) {
        // 💡 MODIFICATION: Use firebaseKey
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

    /**
     * Setup swipe to delete functionality for RecyclerView
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // We don't want to support move functionality
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the position of swiped item
                int position = viewHolder.getAdapterPosition();
                Task task = taskAdapter.getTaskAt(position);

                // Delete the task
                // 💡 MODIFICATION: Use getFirebaseKey()
                deleteTask(task.getFirebaseKey());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}