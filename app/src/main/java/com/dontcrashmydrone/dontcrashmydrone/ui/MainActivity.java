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
                String name = mNameField.getText().toString();
                // Toast.makeText(MainActivity.this, name, Toast.LENGTH_LONG).show();
                startFlying(name);
            }
        });
    }

    private void startFlying(String name){
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra(getString(R.string.Key_name), name);
        startActivity(intent);
    }
}
