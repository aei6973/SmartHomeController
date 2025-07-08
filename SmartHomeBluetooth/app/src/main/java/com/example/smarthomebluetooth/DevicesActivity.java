// DevicesActivity.java (Client)
package com.example.smarthomebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class DevicesActivity extends AppCompatActivity {



    private TextView loadingText;
    private LinearLayout deviceListLayout;
    private final android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        loadingText = findViewById(R.id.loadingText);
        deviceListLayout = findViewById(R.id.deviceListLayout);

        startReceptionThread();
    }

    private void startReceptionThread() {
        new Thread(() -> {
            byte[] buffer = new byte[2048];
            while (true) {
                try {
                    int bytes = ConnectedThread1.getInstance().getInputStream().read(buffer);
                    String message = new String(buffer, 0, bytes);
                    Log.d("BT", "Message JSON client : " + message);
                    JSONArray deviceArray = new JSONArray(message);
                    runOnUiThread(() -> displayDevices(deviceArray));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void displayDevices(JSONArray array) {
        deviceListLayout.removeAllViews();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject device = array.getJSONObject(i);
                String name = device.getString("NAME");
                String model = device.getString("MODEL");
                boolean status = device.getInt("STATE") == 1;
                String id = device.getString("ID");

                deviceListLayout.addView(createDeviceView(name, model, status, id));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View createDeviceView(String name, String model, boolean isOn, String deviceId) {
        RelativeLayout layout = new RelativeLayout(this);

        TextView nameText = new TextView(this);
        nameText.setText(name + " (" + model + ")");
        nameText.setId(View.generateViewId());
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(nameText, nameParams);

        Button toggleButton = new Button(this);
        toggleButton.setText(isOn ? "Éteindre" : "Allumer");
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layout.addView(toggleButton, buttonParams);

        toggleButton.setOnClickListener(v -> {
            try {
                JSONObject message = new JSONObject();
                message.put("deviceId", deviceId);
                message.put("action", "turnOnOff");
                ConnectedThread1.getInstance().write(message.toString());
                Toast.makeText(this, "Commande envoyée", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        layout.setPadding(16, 16, 16, 16);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return layout;
    }
}
