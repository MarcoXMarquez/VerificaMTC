package com.master.verificamtc.admin.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.master.verificamtc.R;
import com.master.verificamtc.helpers.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ListView userListView;
    private FirebaseDatabaseHelper databaseHelper;
    private List<UserData> userDataList;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        userListView = findViewById(R.id.userListView);
        databaseHelper = new FirebaseDatabaseHelper(this);
        userDataList = new ArrayList<>();
        displayUserList();
    }

    private void displayUserList() {
        databaseHelper.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDataList.clear();

                if (!dataSnapshot.exists()) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    FirebaseDatabaseHelper.User user = userSnapshot.getValue(FirebaseDatabaseHelper.User.class);
                    if (user != null) {
                        String userDetails = "DNI: " + user.dni +
                                "\nNombre: " + user.firstName + " " + user.lastName +
                                "\nEmail: " + user.email +
                                "\nEstado: " + getStatusString(user);

                        userDataList.add(new UserData(userDetails, user.dni));
                    }
                }
                UserListAdapter adapter = new UserListAdapter(AdminDashboardActivity.this, userDataList);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error al cargar usuarios: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getStatusString(FirebaseDatabaseHelper.User user) {
        StringBuilder status = new StringBuilder();
        if (user.paymentStatus) status.append("Pago ✓ ");
        if (user.writtenExamPassed) status.append("Teórico ✓ ");
        if (user.drivingExamPassed) status.append("Práctico ✓ ");

        return status.length() > 0 ? status.toString() : "Pendiente";
    }

    // Clase para manejar los datos del usuario
    private static class UserData {
        String details;
        String dni;

        UserData(String details, String dni) {
            this.details = details;
            this.dni = dni;
        }
    }

    // Adaptador integrado
    private class UserListAdapter extends BaseAdapter {
        private Context context;
        private List<UserData> users;

        UserListAdapter(Context context, List<UserData> users) {
            this.context = context;
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
                holder = new ViewHolder();
                holder.tvUserData = convertView.findViewById(R.id.tvUserData);
                holder.btnRegisterFace = convertView.findViewById(R.id.btnRegisterFace);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            UserData user = users.get(position);
            holder.tvUserData.setText(user.details);

            holder.btnRegisterFace.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, FaceDetectionActivity.class);
                startActivity(intent);
            });

            return convertView;
        }

        private class ViewHolder {
            TextView tvUserData;
            Button btnRegisterFace;
        }
    }



}