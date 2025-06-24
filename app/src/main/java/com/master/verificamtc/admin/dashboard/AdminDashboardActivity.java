package com.master.verificamtc.admin.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.master.verificamtc.helpers.AppDatabase;
import com.master.verificamtc.R;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {
    private ListView userListView;
    private AppDatabase databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userListView = findViewById(R.id.userListView);
        databaseHelper = new AppDatabase(this);

        displayUserList();
    }

    private void displayUserList() {
        Cursor cursor = databaseHelper.getAllUsers();
        ArrayList<String> userList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String userData = "ID: " + cursor.getString(0) +
                        "\nNombre: " + cursor.getString(1) +
                        "\nApellido: " + cursor.getString(2) +
                        "\nFecha Nac.: " + cursor.getString(3) +
                        "\nEmail: " + cursor.getString(4);
                userList.add(userData);
            }
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                userList
        );
        userListView.setAdapter(adapter);
    }
}