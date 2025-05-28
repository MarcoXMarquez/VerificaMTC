package com.master.verificamtc;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.*;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class AdminViewActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseScheme myDB;
    ArrayList<String> auth_id, auth_name, auth_last_name, auth_email;
    CustomAdapter customAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userrow);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myDB = new DatabaseScheme(AdminViewActivity.this);

        auth_id = new ArrayList<>();
        auth_name = new ArrayList<>();
        auth_last_name = new ArrayList<>();
        auth_email = new ArrayList<>();

        storeDataInArrays();
        customAdapter = new CustomAdapter(AdminViewActivity.this, auth_id, auth_name, auth_last_name, auth_email);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdminViewActivity.this));
    }

    void storeDataInArrays (){
        Cursor cursor = myDB.readALlData();
        if (cursor.getCount() == 0){
            Toast.makeText(this, "Sin informacion", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                auth_id.add(cursor.getString(0));
                auth_name.add(cursor.getString(1));
                auth_last_name.add(cursor.getString(2));
                auth_email.add(cursor.getString(3));

            }
        }
    }
}