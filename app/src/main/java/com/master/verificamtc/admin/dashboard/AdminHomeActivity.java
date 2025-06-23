package com.master.verificamtc.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.master.verificamtc.R;

public class AdminHomeActivity extends AppCompatActivity {

    private Button btnListaUsuarios;
    private Button btnListaPagos;
    private Button btnGestionHorarios;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Asociar botones
        btnListaUsuarios = findViewById(R.id.btnListaUsuarios);
        btnListaPagos = findViewById(R.id.btnListaPagos);
        btnGestionHorarios = findViewById(R.id.btnGestionHorarios);

        // Eventos de click
        btnListaUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar AdminDashboardActivity que muestra lista de usuarios
                Intent intent = new Intent(AdminHomeActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        btnListaPagos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar PaymentsListActivity que muestra lista de pagos realizados
                Intent intent = new Intent(AdminHomeActivity.this, PaymentsListActivity.class);
                startActivity(intent);
            }
        });

        btnGestionHorarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar ScheduleManagementActivity para gestionar horarios
                Intent intent = new Intent(AdminHomeActivity.this, ScheduleManagementActivity.class);
                startActivity(intent);
            }
        });
    }
}