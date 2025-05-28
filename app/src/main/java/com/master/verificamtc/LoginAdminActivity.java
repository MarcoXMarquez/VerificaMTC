package com.master.verificamtc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class LoginAdminActivity extends AppCompatActivity{
    EditText username, password;
    Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adminlogin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminlogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(username.getText().toString().equals("user") && password.getText().toString().equals("1234")){
                    Toast.makeText(LoginAdminActivity.this, "Inicio de sesion exitoso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginAdminActivity.this, AdminListActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginAdminActivity.this, "Inicio de sesion fallido", Toast.LENGTH_SHORT).show();                }
            }
        });

    }
}