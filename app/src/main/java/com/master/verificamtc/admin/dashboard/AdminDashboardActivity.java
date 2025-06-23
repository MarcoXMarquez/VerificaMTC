package com.master.verificamtc.admin.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.master.verificamtc.R;
import com.master.verificamtc.database.AppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {
    private ListView userListView;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userListView = findViewById(R.id.userListView);
        db = new AppDatabase(this);

        loadUsers();
    }

    private void loadUsers() {
        Cursor cursor = db.getAllUsersWithPaymentStatus();
        List<Map<String, Object>> data = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> row = new HashMap<>();
            int id = cursor.getInt(cursor.getColumnIndex("auth_id"));
            String name = cursor.getString(cursor.getColumnIndex("auth_first_name")) + " " +
                    cursor.getString(cursor.getColumnIndex("auth_last_name"));
            boolean paid = "Sí".equals(cursor.getString(cursor.getColumnIndex("pago")));
            row.put("id", id);
            row.put("name", name);
            row.put("paid", paid);
            data.add(row);
        }
        cursor.close();

        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.item_user_simple, R.id.tvUserName, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tvName = view.findViewById(R.id.tvUserName);
                Button btnToggle = view.findViewById(R.id.btnTogglePay);
                Map<String, Object> item = getItem(position);
                tvName.setText((String) item.get("name"));
                boolean paid = (Boolean) item.get("paid");
                btnToggle.setText(paid ? "Desmarcar Pago" : "Marcar Pago");
                btnToggle.setOnClickListener(v -> {
                    int userId = (Integer) item.get("id");
                    boolean newStatus = !paid;
                    boolean ok = db.updatePaymentStatus(userId, newStatus, "");
                    if (ok) {
                        item.put("paid", newStatus);
                        btnToggle.setText(newStatus ? "Desmarcar Pago" : "Marcar Pago");
                        Toast.makeText(AdminDashboardActivity.this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
                return view;
            }
        };

        userListView.setAdapter(adapter);
    }
}
