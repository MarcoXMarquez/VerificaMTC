package com.master.verificamtc.helpers.facerecognition;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.master.verificamtc.helpers.FirebaseDatabaseHelper;


public class FaceDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyFaces.db";
    public static final String FACE_TABLE_NAME = "faces";
    public static final String FACE_COLUMN_ID = "id";
    public static final String FACE_COLUMN_NAME = "name";
    public static final String FACE_COLUMN_EMBEDDING = "embedding";

    FirebaseDatabaseHelper firebaseHelper;


    public FaceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
         firebaseHelper= new FirebaseDatabaseHelper(context);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // No necesitamos crear la tabla aquí porque ya existe en FirebaseDatabaseHelper
        // Solo usaremos esta base de datos para almacenar embeddings locales
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS faces " +
                        "(id INTEGER PRIMARY KEY, name TEXT, embedding TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS faces");
        onCreate(db);
    }

    public boolean insertFace (String name, Object embedding) {
        float[][] floatList = (float[][]) embedding;
        String embeddingString = "";
        for(Float f: floatList[0]){
            embeddingString+=f.toString()+",";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FACE_COLUMN_NAME, name);
        contentValues.put(FACE_COLUMN_EMBEDDING, embeddingString);
        firebaseHelper.addEmbedding(name, embeddingString);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from faces where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FACE_TABLE_NAME);
        return numRows;
    }

    public boolean updateFace (Integer id, String name, String embedding) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FACE_COLUMN_NAME, name);
        contentValues.put(FACE_COLUMN_EMBEDDING, embedding);
        db.update(FACE_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteFace (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FACE_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    @SuppressLint("Range")
    public HashMap<String, FaceClassifier.Recognition> getAllFaces() {
        HashMap<String, FaceClassifier.Recognition> registered = new HashMap<>();

        // Accedemos a la base de datos sincronizada por FirebaseDatabaseHelper
        SQLiteDatabase db = firebaseHelper.getLocalDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    FirebaseDatabaseHelper.TABLE_FACES,
                    new String[]{
                            FirebaseDatabaseHelper.COLUMN_USER_ID,
                            FirebaseDatabaseHelper.COLUMN_EMBEDDING
                    },
                    null, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String userId = cursor.getString(cursor.getColumnIndex(FirebaseDatabaseHelper.COLUMN_USER_ID));
                    String embeddingString = cursor.getString(cursor.getColumnIndex(FirebaseDatabaseHelper.COLUMN_EMBEDDING));

                    float[][] embeddingArray = convertEmbeddingStringToArray(embeddingString);

                    FaceClassifier.Recognition recognition = new FaceClassifier.Recognition(
                            userId,
                            embeddingArray
                    );
                    registered.put(userId, recognition);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error al leer rostros de la base local", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        Log.d("DBHelper", "Rostros cargados: " + registered.size());
        return registered;
    }

    private float[][] convertEmbeddingStringToArray(String embeddingString) {
        String[] stringValues = embeddingString.split(",");
        float[] floatValues = new float[stringValues.length];

        for (int i = 0; i < stringValues.length; i++) {
            try {
                floatValues[i] = Float.parseFloat(stringValues[i]);
            } catch (NumberFormatException e) {
                floatValues[i] = 0f;
                Log.e("DBHelper", "Error parseando valor: " + stringValues[i]);
            }
        }

        float[][] embeddingArray = new float[1][];
        embeddingArray[0] = floatValues;
        return embeddingArray;
    }

}