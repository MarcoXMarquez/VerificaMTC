package com.master.verificamtc.user.schedule;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;

public class UserScheduleActivity extends AppCompatActivity {

    private TextView tvScheduleInfo;
    private String scheduleId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_schedule);

        tvScheduleInfo = findViewById(R.id.tvScheduleInfo);

        // Intentamos obtener el scheduleId primero
        scheduleId = getIntent().getStringExtra("SCHEDULE_ID");
        userId = getIntent().getStringExtra("USER_ID"); // Fallback por si acaso

        if (scheduleId != null) {
            loadScheduleById();
        } else if (userId != null) {
            loadScheduleByUser();
        } else {
            tvScheduleInfo.setText("No se proporcionó información de horario");
        }
    }

    private void loadScheduleById() {
        FirebaseDatabase.getInstance().getReference()
                .child("schedules")
                .child(scheduleId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String date = dataSnapshot.child("date").getValue(String.class);
                            String time = dataSnapshot.child("time").getValue(String.class);

                            String scheduleInfo = "Horario asignado:\n\n" +
                                    "• Fecha: " + date + "\n" +
                                    "• Hora: " + time;

                            tvScheduleInfo.setText(scheduleInfo);
                        } else {
                            tvScheduleInfo.setText("El horario no existe");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserScheduleActivity.this,
                                "Error al cargar horario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadScheduleByUser() {
        // Mantenemos el método original como fallback
        FirebaseDatabase.getInstance().getReference()
                .child("schedules")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            StringBuilder scheduleInfo = new StringBuilder("Horarios asignados:\n\n");

                            for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                                String date = scheduleSnapshot.child("date").getValue(String.class);
                                String time = scheduleSnapshot.child("time").getValue(String.class);

                                scheduleInfo.append("• Fecha: ").append(date)
                                        .append("\n  Hora: ").append(time)
                                        .append("\n\n");
                            }

                            tvScheduleInfo.setText(scheduleInfo.toString());
                        } else {
                            tvScheduleInfo.setText("No tienes horarios asignados");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserScheduleActivity.this,
                                "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}