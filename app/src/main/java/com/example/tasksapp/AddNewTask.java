package com.example.tasksapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.tasksapp.Model.Todomodel;
import com.example.tasksapp.Utils.dbConnect;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddNewTask extends BottomSheetDialogFragment {
    public static final String TAG = "ActionBottomDialog";

    private EditText newTaskText, newTaskDescription;
    private Button newTaskSaveButton, selectTimeButton;
    private dbConnect db;
    private String selectedTime = "";
    public static AddNewTask newInstance() {
        return new AddNewTask();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTaskText = getView().findViewById(R.id.newTaskText);
        newTaskDescription = getView().findViewById(R.id.newTaskDescription);
        selectTimeButton = getView().findViewById(R.id.selectTimeButton);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);

        db = new dbConnect(getActivity());
        db.openDatabase();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            String description = bundle.getString("description");
            String time = bundle.getString("time");
            newTaskText.setText(task);
            newTaskDescription.setText(description);
            selectedTime = time;
            selectTimeButton.setText("Time: " + time);
            if (task.length() > 0)
                newTaskSaveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        }
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                                selectTimeButton.setText("Time: " + selectedTime);
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });
        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.BLUE);
                } else {
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();
                String description = newTaskDescription.getText().toString();
                String time = selectedTime;

                if (text.isEmpty()) {
                    Toast.makeText(getActivity(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (time.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select a time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (finalIsUpdate) {
                    db.updateTask(bundle.getInt("id"), text, description, time);
                } else {
                    Todomodel task = new Todomodel();
                    task.setTask(text);
                    task.setDescription(description);
                    task.setTime(time);
                    task.setStatus(0);
                    db.addTask(task);

                    scheduleNotification(task);
                }
                dismiss();
            }
        });
    }
    private void scheduleNotification(Todomodel task) {
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        if (intent == null) {
            Log.e("Notification", "Intent is null!");
            return;
        }
        intent.putExtra("task", task.getTask());
        intent.putExtra("description", task.getDescription());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("Notification", "AlarmManager is null!");
            return;
        }
        Calendar calendar = Calendar.getInstance();
        String[] timeParts = task.getTime().split(":");
        if (timeParts.length < 2) {
            Log.e("Notification", "Invalid time format!");
            return;
        }
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener) {
            ((DialogCloseListener) activity).handleDialogClose(dialog);
        }
    }




    }
