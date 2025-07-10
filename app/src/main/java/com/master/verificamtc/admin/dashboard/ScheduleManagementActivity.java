package com.master.verificamtc.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private ArrayList<Schedule> scheduleList;
    private ScheduleAdapter adapter;
    private DatabaseReference scheduleRef;
    private ValueEventListener scheduleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_management);

        scheduleListView = findViewById(R.id.scheduleListView);
        scheduleList = new ArrayList<>();
        adapter = new ScheduleAdapter();
        scheduleListView.setAdapter(adapter);

        scheduleRef = FirebaseDatabase.getInstance().getReference("schedules");

        Button btnAdd = findViewById(R.id.btnAddSchedule);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRealtimeListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (scheduleRef != null && scheduleListener != null) {
            scheduleRef.removeEventListener(scheduleListener);
        }
    }

    private void attachRealtimeListener() {
        if (scheduleListener != null && scheduleRef != null) {
            scheduleRef.removeEventListener(scheduleListener);
        }
        scheduleListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Schedule sch = child.getValue(Schedule.class);
                    if (sch != null) {
                        if (sch.id == null || sch.id.isEmpty())
                            sch.id = child.getKey();
                        scheduleList.add(sch);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScheduleManagementActivity.this, "Error Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        scheduleRef.addValueEventListener(scheduleListener);
    }

    class ScheduleAdapter extends ArrayAdapter<Schedule> {
        ScheduleAdapter() {
            super(ScheduleManagementActivity.this, R.layout.item_schedule, scheduleList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_schedule, parent, false);
            }

            ImageView ivCalendar = convertView.findViewById(R.id.ivScheduleIcon);
            TextView tvDate = convertView.findViewById(R.id.tvScheduleDate);
            TextView tvTime = convertView.findViewById(R.id.tvScheduleTime);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteSchedule);

            Schedule item = getItem(position);
            if (item != null) {
                tvDate.setText(item.date);
                tvTime.setText(item.time);
            }

            boolean isOrange = (position % 2 == 0);
            int bgDrawable = isOrange ? R.drawable.bg_item_schedule_orange : R.drawable.bg_item_schedule_cream;
            int iconTint = isOrange ? R.color.schedule_icon_cream : R.color.schedule_icon_orange;
            convertView.setBackgroundResource(bgDrawable);
            ivCalendar.setImageResource(R.drawable.ic_calendar);
            // Usa app:tint en XML, pero en Java debes usar setColorFilter
            ivCalendar.setColorFilter(ContextCompat.getColor(getContext(), iconTint));

            btnDelete.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        break;
                }
                return false;
            });

            btnDelete.setOnClickListener(v -> {
                if (item != null && item.id != null) {
                    scheduleRef.child(item.id).removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Horario eliminado", Toast.LENGTH_SHORT).show();
                        // No elimines local, Firebase refresca la lista.
                    });
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar. ID no encontrado.", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }
}
