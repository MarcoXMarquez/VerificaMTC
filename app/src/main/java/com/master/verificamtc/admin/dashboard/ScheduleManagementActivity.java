package com.master.verificamtc.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.models.Schedule;

import java.util.ArrayList;

public class ScheduleManagementActivity extends AppCompatActivity {
    private ListView scheduleListView;
    private Button btnAddSchedule;
    private ArrayList<String> scheduleDisplay;
    private ArrayAdapter<String> adapter;
    private DatabaseReference schedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_management);

        scheduleListView = findViewById(R.id.scheduleListView);
        btnAddSchedule   = findViewById(R.id.btnAddSchedule);

        scheduleDisplay = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scheduleDisplay);
        scheduleListView.setAdapter(adapter);

        schedRef = FirebaseDatabase.getInstance().getReference("schedules");

        btnAddSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, AddScheduleActivity.class));
        });

        loadSchedules();
    }

    private void loadSchedules() {
        schedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleDisplay.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Schedule s = child.getValue(Schedule.class);
                    if (s != null) {
                        String entry = "Fecha: " + s.date + " Hora: " + s.time;
                        scheduleDisplay.add(entry);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScheduleManagementActivity.this,
                        "Error Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
