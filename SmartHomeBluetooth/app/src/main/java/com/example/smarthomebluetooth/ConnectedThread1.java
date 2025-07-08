package com.example.smarthomebluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread1 extends Thread {
    private final BluetoothSocket socket;
    private final InputStream inStream;
    private final OutputStream outStream;
    private static ConnectedThread1 instance;

    public ConnectedThread1(BluetoothSocket socket) {
        this.socket = socket;
        instance = this; // ici on enregistre l'instance statique

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("ConnectedThread", "Erreur flux", e);
        }

        inStream = tmpIn;
        outStream = tmpOut;
    }


    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inStream.read(buffer);
                String message = new String(buffer, 0, bytes);
                Log.d("Bluetooth", "Reçu : " + message);
            } catch (IOException e) {
                Log.e("ConnectedThread", "Déconnecté", e);
                break;
            }
        }
    }

    public void write(String message) {
        try {
            outStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e("ConnectedThread", "Erreur d'envoi", e);
        }
    }

    public static ConnectedThread1 getInstance() {
        return instance;
    }
    public InputStream getInputStream() {
        return inStream;
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("ConnectedThread", "Erreur fermeture", e);
        }
    }
}
