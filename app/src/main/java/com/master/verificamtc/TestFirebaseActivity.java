package com.master.verificamtc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.database.FirebaseDatabaseHelper;

public class TestFirebaseActivity extends AppCompatActivity {

    private FirebaseDatabaseHelper dbHelper;
    private EditText etUserId, etFirstName, etLastName, etBirthDate, etEmail, etPassword;
    private TextView tvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_firebase);

        // Inicializar Firebase helper
        dbHelper = new FirebaseDatabaseHelper(this);

        // Obtener referencias de vistas
        etUserId = findViewById(R.id.etUserId);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvUsers = findViewById(R.id.tvUsers);

        Button btnAddUser = findViewById(R.id.btnAddUser);
        Button btnGetUsers = findViewById(R.id.btnGetUsers);
        Button btnUpdatePayment = findViewById(R.id.btnUpdatePayment);

        // Configurar listeners de botones
        btnAddUser.setOnClickListener(v -> addUser());
        btnGetUsers.setOnClickListener(v -> getUsers());
        btnUpdatePayment.setOnClickListener(v -> updatePaymentStatus());
    }

    private void addUser() {
        String userId = etUserId.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.addUser(userId, firstName, lastName, birthDate, email, password);
    }

    private void getUsers() {
        dbHelper.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder usersText = new StringBuilder();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    FirebaseDatabaseHelper.User user = userSnapshot.getValue(FirebaseDatabaseHelper.User.class);
                    if (user != null) {
                        usersText.append("ID: ").append(user.userId).append("\n")
                                .append("Name: ").append(user.firstName).append(" ").append(user.lastName).append("\n")
                                .append("Email: ").append(user.email).append("\n")
                                .append("Payment: ").append(user.paymentStatus ? "Paid" : "Not Paid").append("\n\n");
                    }
                }
                tvUsers.setText(usersText.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TestFirebaseActivity.this, "Failed to read users: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaymentStatus() {
        String userId = etUserId.getText().toString().trim();
        if (userId.isEmpty()) {
            Toast.makeText(this, "Please enter User ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cambiará el estado de pago al opuesto del actual (simulación)
        dbHelper.updatePaymentStatus(userId, true);
        Toast.makeText(this, "Payment status updated for user: " + userId, Toast.LENGTH_SHORT).show();
    }
}