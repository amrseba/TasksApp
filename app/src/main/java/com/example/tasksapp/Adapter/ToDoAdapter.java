package com.example.tasksapp.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tasksapp.AddNewTask;
import com.example.tasksapp.MainActivity;
import com.example.tasksapp.Model.Todomodel;
import com.example.tasksapp.R;
import com.example.tasksapp.Utils.dbConnect;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {
    private List<Todomodel>todoList;
    private MainActivity activity;
    private dbConnect db;
    public ToDoAdapter(dbConnect db, MainActivity activity){
        this.db = db;
        this.activity = activity;
    }
    public ViewHolder onCreateViewHolder(ViewGroup parent , int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (todoList == null || position < 0 || position >= todoList.size()) {
            return;
        }
        db.openDatabase();
        Todomodel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.taskDescription.setText(item.getDescription());
        holder.taskTime.setText(item.getTime());
        holder.task.setChecked(toBoolean(item.getStatus()));

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                db.updateTaskStatus(item.getId(), isChecked ? 1 : 0);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    editItem(adapterPosition);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteItem(adapterPosition);
                }
            }
        });
    }

    public int getItemCount() {
        return todoList.size();
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    public void setTasks(List<Todomodel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }
    public Context getContext() {
        return activity;
    }
    public void deleteItem(int position){
        Todomodel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        Todomodel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("description", item.getDescription());
        bundle.putString("time", item.getTime());

        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }
    // ديه الى هتربط view holder بالview
    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView taskDescription, taskTime;
        Button editButton, deleteButton;



        public ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckbox);
            taskDescription = view.findViewById(R.id.taskDescription);
            taskTime = view.findViewById(R.id.taskTime);
            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);

        }
    }
}
