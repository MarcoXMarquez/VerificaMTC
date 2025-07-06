package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {
    private ListView userListView;
    private List<Map<String, Object>> dataList;
    private ArrayAdapter<Map<String, Object>> adapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userListView = findViewById(R.id.userListView);
        dataList = new ArrayList<>();

        adapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.item_user_simple, dataList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_user_simple, parent, false);
                }

                TextView tvName   = convertView.findViewById(R.id.tvUserName);
                Button   btnToggle = convertView.findViewById(R.id.btnTogglePay);

                Map<String, Object> item = getItem(position);
                String dni   = (String) item.get("dni");
                String name  = (String) item.get("name");
                Boolean paid = (Boolean) item.get("paymentStatus");

                tvName.setText(name);
                btnToggle.setText(paid ? "Desmarcar Pago" : "Marcar Pago");

                btnToggle.setOnClickListener(v -> {
                    DatabaseReference stRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(dni)
                            .child("paymentStatus");

                    stRef.setValue(!paid)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(getContext(), "Pago actualizado", Toast.LENGTH_SHORT).show();
                                // Update local model and refresh list
                                item.put("paymentStatus", !paid);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                });

                return convertView;
            }
        };
        userListView.setAdapter(adapter);

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Cargar users desde Firebase
        loadUsersFromFirebase();
    }

    private void loadUsersFromFirebase() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User u = child.getValue(User.class);
                    if (u != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("dni", u.dni);
                        item.put("name", u.firstName + " " + u.lastName);
                        item.put("paymentStatus", u.paymentStatus);
                        dataList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        AdminDashboardActivity.this,
                        "Error leyendo usuarios: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
