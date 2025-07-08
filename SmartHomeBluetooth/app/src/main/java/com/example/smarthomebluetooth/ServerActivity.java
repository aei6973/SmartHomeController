
package com.example.smarthomebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

import com.example.smarthomebluetooth.ConnectedThread1;

public class ServerActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        // Affichage de l'état
        TextView modeLabel = findViewById(R.id.modeLabel);
        modeLabel.setText("Mode Serveur");

        TextView status = findViewById(R.id.serverStatus);
        status.setText("*Attente de connexion d'un client*");

        // Initialiser l'adaptateur Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non supporté", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startBluetoothServer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBluetoothServer();
            } else {
                Toast.makeText(this, "Bluetooth requis", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startBluetoothServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("SmartHomeBT", MY_UUID);

                runOnUiThread(() -> Toast.makeText(this, "En attente de connexion...", Toast.LENGTH_SHORT).show());

                BluetoothSocket socket = serverSocket.accept();

                BluetoothManager.connectedThread1 = new ConnectedThread1(socket);
                // NE PAS faire .start() ici
                // Lancer l'activité de monitoring
                Intent intent = new Intent(ServerActivity.this, MonitoringActivity.class);
                startActivity(intent);

                serverSocket.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Client connecté !", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erreur serveur Bluetooth", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    public static class ConnectedThread {
    }
}
