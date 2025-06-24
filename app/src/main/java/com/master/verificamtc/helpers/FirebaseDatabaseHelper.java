package com.master.verificamtc.helpers;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDatabaseHelper {
    private DatabaseReference database;
    private Context context;
    public FirebaseDatabaseHelper(Context context) {
        this.context = context;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // Habilita modo offline
        database = FirebaseDatabase.getInstance().getReference();
    }
    public static class User {
        public String dni; // Cambiamos userId por dni
        public String firstName, lastName, birthDate, email, password;
        public boolean paymentStatus, writtenExamPassed, drivingExamPassed;

        public User() {} // Constructor vacío requerido por Firebase

        public User(String dni, String firstName, String lastName,
                    String birthDate, String email, String password) {
            this.dni = dni;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
            this.email = email;
            this.password = password;
            this.paymentStatus = false;
            this.writtenExamPassed = false;
            this.drivingExamPassed = false;
        }
    }
    public static class Car {
        public String vehicleId, userId, color, plate, brand, model;
        public int year;
        public boolean verificationStatus;

        // Constructor vacío requerido por Firebase
        public Car() {
        }

        // Constructor completo
        public Car(String vehicleId, String userId, String color, String plate,
                   String brand, String model, int year) {
            this.vehicleId = vehicleId;
            this.userId = userId;
            this.color = color;
            this.plate = plate;
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.verificationStatus = false; // Por defecto no verificado
        }
    }
    public static class ProcessStatus{
        public String statusId, userId;
        public boolean vehicleCompleted, paymentCompleted, writtenExamPassed, drivingExamPassed;
        public long lastUpdated;

        // Constructor vacío requerido por Firebase
        public ProcessStatus() {}

        // Constructor para nuevo estado
        public ProcessStatus(String userId) {
            this.statusId = FirebaseDatabase.getInstance().getReference()
                    .child("process_status").push().getKey();
            this.userId = userId;
            this.vehicleCompleted = false;
            this.paymentCompleted = false;
            this.writtenExamPassed = false;
            this.drivingExamPassed = false;
            this.lastUpdated = System.currentTimeMillis();
        }

    }

    // Metodo para añadir un usuario

    public void addUser(String dni, String firstName, String lastName,
                        String birthDate, String email, String hashedPassword) {
        // Verificar que el DNI tenga 8 dígitos
        if (!dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("DNI debe tener 8 dígitos");
        }

        // Verificación adicional del hash
        if (!hashedPassword.startsWith("$2a$")) {
            throw new SecurityException("Formato de hash inválido");
        }

        // Usamos el DNI como ID principal
        User newUser = new User(dni, firstName, lastName, birthDate, email, hashedPassword);

        database.child("users").child(dni).setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Registro completado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error en registro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    public void getAllUsers(ValueEventListener listener) {
        database.child("users").addValueEventListener(listener);
    }
    public void updatePaymentStatus(String userId, boolean isPaid) {
        database.child("users").child(userId).
                child("paymentStatus").setValue(isPaid);
    }

    // Clase modelo para User

}