package com.guideme.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Places extends AppCompatActivity {
    // Variables
    public static String fullName = "";
    public static String homeAddress = "";
    public static String workAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        Button setButton = (Button) findViewById(R.id.setButton);
        Button backButton = (Button) findViewById(R.id.backButton);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings();

                while(checkInvalidResponse() != true)
                {
                    setSettings();
                }

                startActivity(new Intent(Places.this, Home.class));
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Places.this, Home.class));
            }
        });

    }

    public void setSettings()
    {
        fullName = findViewById(R.id.fullName).toString();
        homeAddress = findViewById(R.id.homeAddress).toString();
        workAddress = findViewById(R.id.workAddress).toString();
    }

    public String toString(EditText newText)
    {
        return newText.getText().toString();
    }

    public boolean checkInvalidResponse()
    {
        if(fullName.isEmpty() || homeAddress.isEmpty() || workAddress.isEmpty())
            return false;
        else
            return true;
    }
}