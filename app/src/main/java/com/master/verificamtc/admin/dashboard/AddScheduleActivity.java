package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.verificamtc.R;
import com.master.verificamtc.models.Schedule;

public class AddScheduleActivity extends AppCompatActivity {
    private EditText etFecha, etHora;
    private Button btnSaveSchedule;
    private DatabaseReference schedulesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        etFecha = findViewById(R.id.etFecha);
        etHora  = findViewById(R.id.etHora);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);

        schedulesRef = FirebaseDatabase.getInstance()
                .getReference("schedules");

        btnSaveSchedule.setOnClickListener(v -> {
            String fecha = etFecha.getText().toString().trim();
            String hora  = etHora.getText().toString().trim();
            if(fecha.isEmpty()||hora.isEmpty()){
                Toast.makeText(this,"Complete fecha y hora",Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("schedules");
            String key = ref.push().getKey();
            Schedule s = new Schedule(key, fecha, hora);
            ref.child(key).setValue(s)
                    .addOnSuccessListener(a->{
                        Toast.makeText(this,"Horario agregado en Firebase",Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e->
                            Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show()
                    );
        });

    }
}
