package com.example.n3023685.gpspractice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.ERROR_MESSAGE);
        TextView textView = findViewById(R.id.errorBox);
        textView.setText(message);
    }

}
