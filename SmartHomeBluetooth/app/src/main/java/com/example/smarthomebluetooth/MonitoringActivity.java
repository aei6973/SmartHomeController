// MonitoringActivity.java - Côté serveur (Hub)
package com.example.smarthomebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MonitoringActivity extends AppCompatActivity {
    private LinearLayout deviceListLayout;
    private Handler handler = new Handler();
    private final String url = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/6";
    private BluetoothAdapter bluetoothAdapter;
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Handler de mise à jour périodique toutes les 10s
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchDevices();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        deviceListLayout = findViewById(R.id.deviceListLayout);

        // Initialisation Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non supporté", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Thread serveur pour attendre la connexion client
        new Thread(() -> {
            try {
                // Création du socket serveur avec nom et UUID partagé
                BluetoothServerSocket serverSocket = bluetoothAdapter
                        .listenUsingRfcommWithServiceRecord("SmartHomeBT", MY_UUID);

                // Attente bloquante d'une connexion client
                BluetoothSocket socket = serverSocket.accept();
                serverSocket.close();

                // Lancement du thread de communication Bluetooth
                ConnectedThread1 thread = new ConnectedThread1(socket);
                thread.start();

                // Une fois connecté, on peut rafraîchir l'affichage et envoyer au client
                runOnUiThread(this::fetchDevices);
            } catch (IOException e) {
                Log.e("BT", "Erreur socket serveur", e);
            }
        }).start();

        // Thread permanent pour réception des commandes JSON côté serveur
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytes = ConnectedThread1.getInstance().getInputStream().read(buffer);
                    String message = new String(buffer, 0, bytes);

                    runOnUiThread(() -> Toast.makeText(this, "Commande reçue : " + message, Toast.LENGTH_SHORT).show());

                    try {
                        JSONObject json = new JSONObject(message);
                        String deviceId = json.getString("deviceId");
                        String action = json.getString("action");

                        envoyerRequetePOST(deviceId, action);
                    } catch (JSONException e) {
                        Log.e("BT", "Erreur parsing JSON", e);
                    }

                } catch (IOException | NullPointerException e) {
                    Log.e("BT", "Erreur lecture Bluetooth", e);
                    break;
                }
            }
        }).start();
    }

    // Méthode utilisée pour envoyer la liste JSON vers le client
    private void envoyerAppareilsAuClient(JSONArray devices) {
        if (ConnectedThread1.getInstance() != null) {
            ConnectedThread1.getInstance().write(devices.toString());
            Log.d("BT", "Liste envoyée au client : " + devices.toString());
        }
    }

    // Récupération de la liste des appareils via API REST
    private void fetchDevices() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    deviceListLayout.removeAllViews();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject device = response.getJSONObject(i);
                            String name = device.getString("NAME");
                            String model = device.getString("MODEL");
                            boolean status = device.getInt("STATE") == 1;
                            String id = device.getString("ID");

                            View deviceView = createDeviceView(name, model, status, id);
                            deviceListLayout.addView(deviceView);
                        }

                        // ENVOI AU CLIENT APRÈS AFFICHAGE LOCAL
                        envoyerAppareilsAuClient(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(MonitoringActivity.this, "Erreur API REST", Toast.LENGTH_SHORT).show();
                    Log.e("API", "Erreur Volley : " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("API", "Code HTTP : " + error.networkResponse.statusCode);
                    }
                });

        queue.add(jsonArrayRequest);
    }

    // Génération dynamique de la vue d’un appareil dans la liste
    private View createDeviceView(String name, String model, boolean isOn, String deviceId) {
        RelativeLayout layout = new RelativeLayout(this);

        TextView nameText = new TextView(this);
        nameText.setText(name + " (" + model + ")");
        nameText.setId(View.generateViewId());

        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(nameText, nameParams);

        TextView statusText = new TextView(this);
        statusText.setText(isOn ? "[ON]" : "[OFF]");
        statusText.setId(View.generateViewId());

        RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        statusParams.addRule(RelativeLayout.BELOW, nameText.getId());
        layout.addView(statusText, statusParams);

        layout.setPadding(16, 16, 16, 16);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return layout;
    }

    // Envoi d’une commande REST à l’API suite à une requête du client
    private void envoyerRequetePOST(String deviceId, String action) {
        String url = "http://happyresto.enseeiht.fr/smartHouse/api/v1/devices/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("BT", "Commande exécutée : " + response);
                    fetchDevices(); // Rafraîchit et réenvoie au client
                },
                error -> {
                    Log.e("BT", "Erreur REST : " + error.toString());
                    Toast.makeText(this, "Erreur de traitement de la commande", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("houseId", "6");
                params.put("deviceId", deviceId);
                params.put("action", action);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(postRequest);
    }
}
