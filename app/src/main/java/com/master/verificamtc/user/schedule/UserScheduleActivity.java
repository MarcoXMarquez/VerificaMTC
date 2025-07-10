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
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_schedule);

        tvScheduleInfo = findViewById(R.id.tvScheduleInfo);
        userId = getIntent().getStringExtra("USER_DNI");

        loadScheduleData();
    }

    private void loadScheduleData() {
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("schedules")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            StringBuilder scheduleInfo = new StringBuilder("Horarios asignados:\n\n");

                            for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                                String scheduleId = scheduleSnapshot.getKey();

                                // Obtener detalles del horario
                                FirebaseDatabase.getInstance().getReference()
                                        .child("schedules")
                                        .child(scheduleId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot scheduleDetailSnapshot) {
                                                if (scheduleDetailSnapshot.exists()) {
                                                    String date = scheduleDetailSnapshot.child("date").getValue(String.class);
                                                    String time = scheduleDetailSnapshot.child("time").getValue(String.class);

                                                    scheduleInfo.append("• Fecha: ").append(date)
                                                            .append("\n  Hora: ").append(time)
                                                            .append("\n\n");

                                                    tvScheduleInfo.setText(scheduleInfo.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(UserScheduleActivity.this,
                                                        "Error al cargar horario", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
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