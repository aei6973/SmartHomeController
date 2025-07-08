package com.example.smarthomebluetooth;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonClient = findViewById(R.id.buttonClient);
        Button buttonServeur = findViewById(R.id.buttonServer);

        buttonClient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClientActivity.class);
            startActivity(intent);
        });

        buttonServeur.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ServerActivity.class);
            startActivity(intent);
        });
    }
}
