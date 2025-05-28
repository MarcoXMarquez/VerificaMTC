package com.master.verificamtc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    private ArrayList auth_id, auth_name, auth_last_name, auth_email;
    private Activity activity;

    CustomAdapter(Context context,
                    ArrayList auth_id,
                    ArrayList auth_name,
                    ArrayList auth_last_name ,
                    ArrayList auth_email
                    ){
        this.context = context;
        this.auth_id = auth_id;
        this.auth_name = auth_name;
        this.auth_last_name = auth_last_name;
        this.auth_email = auth_email;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_userrow, parent , false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.user_id_txt.setText(String.valueOf(auth_id.get(position)));
        holder.user_name_txt.setText(String.valueOf(auth_name.get(position)));
        holder.user_lastname_txt.setText(String.valueOf(auth_last_name.get(position)));
        holder.user_email_txt.setText(String.valueOf(auth_email.get(position)));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView user_id_txt,user_name_txt, user_lastname_txt, user_email_txt;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            user_id_txt = itemView.findViewById(R.id.user_id_txt);
            user_name_txt = itemView.findViewById(R.id.user_name_txt);
            user_lastname_txt = itemView.findViewById(R.id.user_lastname_txt);
            user_email_txt = itemView.findViewById(R.id.user_email_txt);
        }
    }
}
