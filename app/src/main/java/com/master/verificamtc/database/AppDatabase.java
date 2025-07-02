package com.master.verificamtc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AppDatabase extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Verificamtc.db";
    private static final int DATABASE_VERSION = 3; // Incrementamos la versión por los cambios

    // Tabla de autenticación (existente)
    public static final String TABLE_AUTH = "auth_registry";
    public static final String COLUMN_ID = "auth_id";
    public static final String COLUMN_NAMES = "auth_first_name";
    public static final String COLUMN_LASTNAMES = "auth_last_name";
    public static final String COLUMN_BIRTHDATE = "auth_birth_date";
    public static final String COLUMN_EMAIL = "auth_email";
    public static final String COLUMN_PASSWORD = "auth_password";

    // Nueva tabla: Información del vehículo
    public static final String TABLE_VEHICLE = "vehicle_info";
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_USER_ID = "user_id"; // FK a auth_registry
    public static final String COLUMN_COLOR = "vehicle_color";
    public static final String COLUMN_PLATE = "vehicle_plate";
    public static final String COLUMN_BRAND = "vehicle_brand";
    public static final String COLUMN_MODEL = "vehicle_model";
    public static final String COLUMN_YEAR = "vehicle_year";

    // Nueva tabla: Estado del trámite
    public static final String TABLE_STATUS = "process_status";
    public static final String COLUMN_STATUS_ID = "status_id";
    public static final String COLUMN_HAS_PAID = "has_paid";
    public static final String COLUMN_WRITTEN_EXAM_PASSED = "written_exam_passed";
    public static final String COLUMN_PRACTICAL_EXAM_PASSED = "practical_exam_passed";
    public static final String COLUMN_PAYMENT_DATE = "payment_date";
    public static final String COLUMN_EXAM_DATE = "exam_date";

    // Nueva tabla: Horarios
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String COLUMN_SCHEDULE_ID = "schedule_id";
    public static final String COLUMN_SCHEDULE_DATE = "fecha";
    public static final String COLUMN_SCHEDULE_TIME = "hora";

    public AppDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de autenticación
        String createAuthTable = "CREATE TABLE " + TABLE_AUTH + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAMES + " TEXT NOT NULL, " +
                COLUMN_LASTNAMES + " TEXT NOT NULL, " +
                COLUMN_BIRTHDATE + " DATE, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT NOT NULL" +
                ");";
        db.execSQL(createAuthTable);

        // Crear tabla de vehículo
        String createVehicleTable = "CREATE TABLE " + TABLE_VEHICLE + " (" +
                COLUMN_VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_COLOR + " TEXT NOT NULL, " +
                COLUMN_PLATE + " TEXT NOT NULL UNIQUE, " +
                COLUMN_BRAND + " TEXT, " +
                COLUMN_MODEL + " TEXT, " +
                COLUMN_YEAR + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_AUTH + "(" + COLUMN_ID + ")" +
                ");";
        db.execSQL(createVehicleTable);

        // Crear tabla de estado
        String createStatusTable = "CREATE TABLE " + TABLE_STATUS + " (" +
                COLUMN_STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL UNIQUE, " +
                COLUMN_HAS_PAID + " INTEGER DEFAULT 100, " +
                COLUMN_PAYMENT_DATE + " TEXT, " +
                COLUMN_WRITTEN_EXAM_PASSED + " INTEGER DEFAULT 0, " +
                COLUMN_PRACTICAL_EXAM_PASSED + " INTEGER DEFAULT 0, " +
                COLUMN_EXAM_DATE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_AUTH + "(" + COLUMN_ID + ")" +
                ");";
        db.execSQL(createStatusTable);

        // Crear tabla de horarios (agregado)
        String createSchedulesTable = "CREATE TABLE " + TABLE_SCHEDULES + " (" +
                COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCHEDULE_DATE + " TEXT, " +
                COLUMN_SCHEDULE_TIME + " TEXT" +
                ");";
        db.execSQL(createSchedulesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES); // eliminado schedules
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTH);
        onCreate(db);
    }

    // Métodos para la tabla de autenticación (existente)
    public void addAuth(int id, String firstName, String lastName, String birthDate, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, id);
        cv.put(COLUMN_NAMES, firstName);
        cv.put(COLUMN_LASTNAMES, lastName);
        cv.put(COLUMN_BIRTHDATE, birthDate);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_AUTH, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Registro fallido", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show();
            initializeUserStatus(id);
        }
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_AUTH,
                new String[]{COLUMN_ID, COLUMN_NAMES, COLUMN_LASTNAMES, COLUMN_BIRTHDATE, COLUMN_EMAIL},
                null, null, null, null, null
        );
    }

    // Métodos para la tabla de vehículo (existente)
    public boolean addVehicle(int userId, String color, String plate, String brand, String model, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_COLOR, color);
        cv.put(COLUMN_PLATE, plate);
        cv.put(COLUMN_BRAND, brand);
        cv.put(COLUMN_MODEL, model);
        cv.put(COLUMN_YEAR, year);
        try {
            long result = db.insert(TABLE_VEHICLE, null, cv);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public boolean updateVehicle(int userId, String color, String plate, String brand, String model, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COLOR, color);
        cv.put(COLUMN_PLATE, plate);
        cv.put(COLUMN_BRAND, brand);
        cv.put(COLUMN_MODEL, model);
        cv.put(COLUMN_YEAR, year);
        try {
            int rowsAffected = db.update(
                    TABLE_VEHICLE,
                    cv,
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public Cursor getVehicleByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_VEHICLE,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
    }

    // Métodos para estado de trámite (existente)
    public boolean initializeUserStatus(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_ID, userId);
        long result = db.insert(TABLE_STATUS, null, cv);
        return result != -1;
    }

    public boolean updatePaymentStatus(int userId, boolean hasPaid, String paymentDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_HAS_PAID, hasPaid ? 1 : 0);
        cv.put(COLUMN_PAYMENT_DATE, paymentDate);

        int rowsAffected = db.update(
                TABLE_STATUS,
                cv,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        if (rowsAffected == 0) {
            initializeUserStatus(userId);
            rowsAffected = db.update(
                    TABLE_STATUS,
                    cv,
                    COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) }
            );
        }

        return rowsAffected > 0;
    }


    public boolean updateExamStatus(int userId, boolean writtenPassed, boolean practicalPassed, String examDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WRITTEN_EXAM_PASSED, writtenPassed ? 1 : 0);
        cv.put(COLUMN_PRACTICAL_EXAM_PASSED, practicalPassed ? 1 : 0);
        cv.put(COLUMN_EXAM_DATE, examDate);
        int rowsAffected = db.update(
                TABLE_STATUS,
                cv,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        return rowsAffected > 0;
    }

    public Cursor getUserStatus(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_STATUS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
    }

    // MÉTODOS AGREGADOS PARA LISTADOS
    /** Obtiene todos los usuarios con estado de pago */
    public Cursor getAllUsersWithPaymentStatus() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COLUMN_ID + ", u." + COLUMN_NAMES + ", u." + COLUMN_LASTNAMES + ", u." + COLUMN_BIRTHDATE + ", u." + COLUMN_EMAIL + ", " +
                "CASE WHEN p." + COLUMN_HAS_PAID + " = 1 THEN 'Sí' ELSE 'No' END AS pago " +
                "FROM " + TABLE_AUTH + " u " +
                "LEFT JOIN " + TABLE_STATUS + " p ON u." + COLUMN_ID + " = p." + COLUMN_USER_ID;
        return db.rawQuery(query, null);
    }

    /** Obtiene todos los pagos registrados */
    public Cursor getAllPayments() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT p." + COLUMN_STATUS_ID + " AS id, " +
                        "u." + COLUMN_NAMES + " || ' ' || u." + COLUMN_LASTNAMES + " AS usuario, " +
                        "p." + COLUMN_HAS_PAID + " AS monto, " +        // <-- nuevo
                        "p." + COLUMN_PAYMENT_DATE + " AS fecha " +
                        "FROM " + TABLE_STATUS + " p " +
                        "LEFT JOIN " + TABLE_AUTH + " u ON p." + COLUMN_USER_ID + " = u." + COLUMN_ID;
        return db.rawQuery(query, null);
    }

    /** Obtiene todos los horarios disponibles */
    public Cursor getAllSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SCHEDULES, null);
    }

    /** Inserta un nuevo horario */
    public long addSchedule(String fecha, String hora) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SCHEDULE_DATE, fecha);
        cv.put(COLUMN_SCHEDULE_TIME, hora);
        return db.insert(TABLE_SCHEDULES, null, cv);
    }
}
