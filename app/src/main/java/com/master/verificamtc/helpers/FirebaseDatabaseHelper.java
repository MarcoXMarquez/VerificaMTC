package com.master.verificamtc.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
        public String dni;
        public String firstName, lastName, birthDate, email, password;
        public boolean paymentStatus, drivingExamPassed; // Eliminado writtenExamPassed
        public boolean hasVehicle;
        public Map<String, Boolean> schedules;

        public User() {}

        public User(String dni, String firstName, String lastName,
                    String birthDate, String email, String password) {
            this.dni = dni;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
            this.email = email;
            this.password = password;
            this.paymentStatus = false;
            this.drivingExamPassed = false; // Solo examen práctico
            this.hasVehicle = false;
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

    public void addVehicle(Car vehicle, OnCompleteListener<Void> completionListener) {
        if (vehicle == null || vehicle.plate == null || vehicle.plate.isEmpty()) {
            Toast.makeText(context, "Placa del vehículo inválida", Toast.LENGTH_SHORT).show();
            if (completionListener != null) {
                // Simplemente no llamamos al listener en caso de error temprano
                // O puedes crear un Task fallido usando Tasks.forException() si tienes acceso a él
            }
            return;
        }

        String normalizedPlate = vehicle.plate.replace("-", "_");

        database.child("users").child(vehicle.userId)
                .child("vehicles").child(normalizedPlate)
                .setValue(vehicle)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        database.child("users").child(vehicle.userId)
                                .child("hasVehicle").setValue(true)
                                .addOnCompleteListener(updateTask -> {
                                    if (completionListener != null) {
                                        completionListener.onComplete(updateTask);
                                    }
                                    // Mostrar mensaje de éxito/error
                                });
                    } else {
                        if (completionListener != null) {
                            completionListener.onComplete(task);
                        }
                        // Mostrar mensaje de error
                    }
                });
    }
    public void getVehicleByPlate(String plate, ValueEventListener listener) {
        String normalizedPlate = plate.replace("-", "_");
        database.child("vehicles").child(normalizedPlate).addListenerForSingleValueEvent(listener);
    }

    public DatabaseReference getDatabaseReference() {
        return this.database;
    }
    // ====== NUEVAS ADICIONES ======
    public static class Question {
        public String id;
        public String text;
        public String zoneType;

        public Question() {}

        public Question(String id, String text, String zoneType) {
            this.id = id;
            this.text = text;
            this.zoneType = zoneType;
        }
    }

    public static class Answer {
        public String userId;
        public String questionId;
        public int rating;
        public long timestamp;

        public Answer() {}

        public Answer(String userId, String questionId, int rating) {
            this.userId = userId;
            this.questionId = questionId;
            this.rating = rating;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public void getQuestions(String zoneType, ValueEventListener listener) {
        database.child("questions").child(zoneType)
                .addListenerForSingleValueEvent(listener);
    }

    // Versión actualizada para guardar respuestas como objetos Answer
    public void saveAnswers(String userId, String zoneType, Map<String, Object> answers) {
        // Primero validamos los datos
        if (userId == null || zoneType == null || answers == null) {
            Log.e("Firebase", "Datos inválidos para guardar respuestas");
            return;
        }

        // Creamos un mapa con la estructura correcta
        Map<String, Object> answerData = new HashMap<>();

        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q")) { // q1, q2, q3
                Object value = entry.getValue();

                // Creamos un objeto Answer para cada respuesta
                Map<String, Object> answer = new HashMap<>();
                answer.put("rating", value);
                answer.put("timestamp", System.currentTimeMillis());

                answerData.put(key, answer);
            }
        }

        // Añadimos timestamp general
        answerData.put("timestamp", System.currentTimeMillis());

        // Guardamos en Firebase
        database.child("answers")
                .child(userId)
                .child(zoneType)
                .setValue(answerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Respuestas guardadas correctamente");
                    // Verifica en la consola de Firebase que los datos se actualizaron
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error al guardar respuestas", e);
                });
    }


    public void getAnswers(String userId, String zoneType, ValueEventListener listener) {
        database.child("answers").child(userId).child(zoneType)
                .addListenerForSingleValueEvent(listener);
    }

    // Versión compatible con tu estructura actual de Firebase (strings directos)
    public void initializeDefaultQuestions() {
        Map<String, Object> questions = new HashMap<>();

        // Preguntas para curva (strings directos)
        Map<String, Object> curveQuestions = new HashMap<>();
        curveQuestions.put("q1", "¿Redujo la velocidad al ingresar a la curva?");
        curveQuestions.put("q2", "¿Mantuvo el control del vehículo durante la curva?");
        curveQuestions.put("q3", "¿Usó adecuadamente los espejos y señalización?");

        // Preguntas para estacionamiento (strings directos)
        Map<String, Object> parkingQuestions = new HashMap<>();
        parkingQuestions.put("q1", "¿Respetó la señalización?");
        parkingQuestions.put("q2", "¿Cedió el paso correctamente?");
        parkingQuestions.put("q3", "¿Mantiene distancia de seguridad?");

        questions.put("curve", curveQuestions);
        questions.put("parking", parkingQuestions);

        database.child("questions").setValue(questions)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firebase", "Preguntas (strings) inicializadas"))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Error al inicializar preguntas", e));
    }
    // En FirebaseDatabaseHelper.java
    public interface ScheduleCompletionListener {
        void onSuccess(String scheduleId);
        void onFailure(String errorMessage);
    }

    public void addSchedule(String userId, String date, String time, ScheduleCompletionListener listener) {
        String scheduleId = database.child("schedules").push().getKey();

        if (scheduleId == null) {
            if (listener != null) {
                listener.onFailure("No se pudo generar ID de horario");
            }
            return;
        }

        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("date", date);
        scheduleData.put("time", time);
        scheduleData.put("userId", userId);

        database.child("schedules").child(scheduleId)
                .setValue(scheduleData)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess(scheduleId);

                        // Opcional: Guardar referencia en el usuario
                        database.child("users").child(userId)
                                .child("schedules").child(scheduleId)
                                .setValue(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }
}