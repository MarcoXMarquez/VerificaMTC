package com.master.verificamtc.admin.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;

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

                ImageView ivIcon   = convertView.findViewById(R.id.ivUserIcon);
                TextView  tvName   = convertView.findViewById(R.id.tvUserName);
                Button    btnToggle= convertView.findViewById(R.id.btnTogglePay);

                Map<String,Object> item = getItem(position);
                String dni            = (String) item.get("dni");
                String name           = (String) item.get("name");
                Boolean paymentStatus = (Boolean) item.get("paymentStatus");

                tvName.setText(name);
                // Cambia el icono según el estado
                ivIcon.setImageResource(
                        paymentStatus
                                ? R.drawable.user_payment_2   // pagó
                                : R.drawable.user_payment_1    // no pagó
                );
                btnToggle.setText(paymentStatus ? "Desmarcar Pago" : "Marcar Pago");

                // Cambia color del botón según el estado
                int orange = ContextCompat.getColor(getContext(), R.color.button_orange); // En colors.xml
                int aqua   = ContextCompat.getColor(getContext(), R.color.button_aqua);   // En colors.xml
                btnToggle.setBackgroundTintList(ColorStateList.valueOf(
                        paymentStatus ? aqua : orange
                ));

                // Animación al tocar el botón
                btnToggle.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            break;
                    }
                    return false;
                });

                btnToggle.setOnClickListener(v -> {
                    boolean current = (Boolean) item.get("paymentStatus");
                    boolean newStatus = !current;
                    DatabaseReference stRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(dni)
                            .child("paymentStatus");

                    stRef.setValue(newStatus)
                            .addOnSuccessListener(a -> {
                                item.put("paymentStatus", newStatus);
                                ivIcon.setImageResource(
                                        newStatus ? R.drawable.user_payment_2 : R.drawable.user_payment_1
                                );
                                btnToggle.setText(newStatus ? "Desmarcar Pago" : "Marcar Pago");
                                btnToggle.setBackgroundTintList(ColorStateList.valueOf(
                                        newStatus ? aqua : orange
                                ));
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
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
