
package com.example.smarthomebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import com.example.smarthomebluetooth.ConnectedThread1;

public class ClientActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        TextView modeLabel = findViewById(R.id.modeLabel);
        modeLabel.setText("Mode Client");

        TextView status = findViewById(R.id.clientStatus);
        status.setText("*Attente de connexion au serveur*");

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
            connectToServer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToServer();
            } else {
                Toast.makeText(this, "Bluetooth requis", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Aucun appareil appairé trouvé", Toast.LENGTH_LONG).show());
                    return;
                }

                BluetoothDevice serverDevice = pairedDevices.iterator().next();
                BluetoothSocket socket = serverDevice.createRfcommSocketToServiceRecord(MY_UUID);

                socket.connect();

                BluetoothManager.connectedThread1 = new ConnectedThread1(socket);
                // NE PAS faire .start() ici


                runOnUiThread(() -> {
                    Toast.makeText(this, "Connecté au serveur !", Toast.LENGTH_SHORT).show();

                    //  Lancement de DevicesActivity
                    Intent intent = new Intent(ClientActivity.this, DevicesActivity.class);
                    startActivity(intent);
                    finish(); //pour fermer ClientActivity
                });


            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Échec de connexion au serveur", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
