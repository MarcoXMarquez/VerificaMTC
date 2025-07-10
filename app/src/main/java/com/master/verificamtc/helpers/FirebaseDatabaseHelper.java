package com.master.verificamtc.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDatabaseHelper extends SQLiteOpenHelper {
    private DatabaseReference database;
    private Context context;
    private static final String DATABASE_NAME = "faces_db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_FACES = "faces";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMBEDDING = "embedding";
    public FirebaseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        database = FirebaseDatabase.getInstance().getReference();
    }
    public SQLiteDatabase getLocalDatabase() {
        return this.getReadableDatabase();
    }
    public interface SyncCompletionListener {
        void onSyncComplete(boolean success);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FACES_TABLE = "CREATE TABLE " + TABLE_FACES + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                + COLUMN_EMBEDDING + " TEXT)";
        db.execSQL(CREATE_FACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACES);
        onCreate(db);
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
    public void addEmbedding(String userId, String embedding ){
        database.child("embeddings").child(userId).setValue(embedding);
    }
    public void getAllFaces( SyncCompletionListener listener) {
        DatabaseReference embeddingsRef = database.child("embeddings");

        embeddingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SQLiteDatabase db = getWritableDatabase();
                boolean success = false;

                try {
                    db.beginTransaction();
                    db.delete(TABLE_FACES, null, null); // Limpiar tabla existente

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String embedding = userSnapshot.getValue(String.class);

                        ContentValues values = new ContentValues();
                        values.put(COLUMN_USER_ID, userId);
                        values.put(COLUMN_EMBEDDING, embedding);

                        db.insert(TABLE_FACES, null, values);
                    }

                    db.setTransactionSuccessful();
                    success = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                } finally {
                    db.endTransaction();
                    db.close();

                    // Notificar que la sincronización ha terminado
                    if (listener != null) {
                        listener.onSyncComplete(success);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (listener != null) {
                    listener.onSyncComplete(false);
                }
            }
        });
    }
    public void getAllUsers(ValueEventListener listener) {
        database.child("users").addValueEventListener(listener);
    }
    public void updatePaymentStatus(String userId, boolean isPaid) {
        database.child("users").child(userId).
                child("paymentStatus").setValue(isPaid);
    }

    public void addVehicle(Car vehicle) {
        if (vehicle == null || vehicle.plate == null || vehicle.plate.isEmpty()) {
            Toast.makeText(context, "Placa del vehículo inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalizar placa para clave de Firebase
        String normalizedPlate = vehicle.plate.replace("-", "_");

        database.child("vehicles").child(normalizedPlate)
                .setValue(vehicle)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar referencia en usuario si existe
                    if (vehicle.userId != null && !vehicle.userId.isEmpty()) {
                        database.child("users").child(vehicle.userId)
                                .child("vehicles").child(normalizedPlate)
                                .setValue(true);
                    }
                    Toast.makeText(context, "Vehículo registrado exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al registrar vehículo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void getVehicleByPlate(String plate, ValueEventListener listener) {
        String normalizedPlate = plate.replace("-", "_");
        database.child("vehicles").child(normalizedPlate).addListenerForSingleValueEvent(listener);
    }

    public DatabaseReference getDatabaseReference() {
        return this.database;
    }

}