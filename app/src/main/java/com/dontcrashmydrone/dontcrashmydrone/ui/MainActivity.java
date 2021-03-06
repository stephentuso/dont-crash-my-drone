package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dontcrashmydrone.dontcrashmydrone.R;

public class MainActivity extends AppCompatActivity {

    private EditText mNameField;
    private Button mStartButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameField = (EditText) findViewById(R.id.nameEditText);
        mStartButton = (Button) findViewById(R.id.startButton);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This activity is started from StartFlightActivity now, all we need to do is close it
                //TODO: Save in sharedPrefs that the user has completed this (and then check that in StartFlightActivity)
                finish();
            }
        });
    }
}
