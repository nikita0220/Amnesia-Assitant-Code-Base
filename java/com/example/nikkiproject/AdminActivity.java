package com.example.nikkiproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AdminActivity extends AppCompatActivity {

    private LinearLayout loginLayout = null;
    private LinearLayout mainLayout = null;
    private EditText pass = null;
    private String ADMIN_PASSWORD = "test";
    //private Context context = getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        pass = findViewById(R.id.admin_password);

        loginLayout = findViewById(R.id.linearLayout1);
        mainLayout = findViewById(R.id.linearLayout2);

        mainLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);

    }

    public void adminLogin(View view) {
        //Log.v("Nikki", String.valueOf(pass.getText()));
        if (String.valueOf(pass.getText()).equals(ADMIN_PASSWORD)) {
            mainLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        }
        else {
            Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
        }
    }

    public void addPersonClick(View view) {
        Intent addPerson = new Intent(this, AddPerson.class);
        startActivity(addPerson);
    }

    public void editPersonClick(View view) {
        Intent addPerson = new Intent(this, AddPerson.class);
        startActivity(addPerson);
    }

}