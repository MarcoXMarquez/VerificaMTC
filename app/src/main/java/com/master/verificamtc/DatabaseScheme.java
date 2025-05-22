package com.master.verificamtc;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

class DatabaseScheme extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME="Verificamtc.db";
    private static final int DATABASE_VERSION=1;

    private static final String TABLE_NAME = "auth_registry";
    private static final String COLUMN_ID = "auth_id";
    private static final String COLUMN_NAMES = "auth_first_name";
    private static final String COLUMN_LASTNAMES = "auth_last_name";
    private static final String COLUMN_BIRTHDATE = "auth_birth_date";
    private static final String COLUMN_EMAIL = "auth_email";
    private static final String COLUMN_PASSWORD = "auth_password";

    DatabaseScheme(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAMES + " TEXT NOT NULL, " +
                        COLUMN_LASTNAMES + " TEXT NOT NULL, " +
                        COLUMN_BIRTHDATE + " DATE, " +
                        COLUMN_EMAIL + " TEXT UNIQUE, " +
                        COLUMN_PASSWORD + " TEXT NOT NULL" +
                        ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    void addAuth(int id, String firstName, String lastName, String birthDate, String email, String password) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ID, id);
        cv.put(COLUMN_NAMES, firstName);
        cv.put(COLUMN_LASTNAMES, lastName);
        cv.put(COLUMN_BIRTHDATE, birthDate);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_NAME, null, cv);
        if(result == -1){
            Toast.makeText(context, "Intento Fallido", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Registro Existoso", Toast.LENGTH_SHORT).show();
        }
    }
}
