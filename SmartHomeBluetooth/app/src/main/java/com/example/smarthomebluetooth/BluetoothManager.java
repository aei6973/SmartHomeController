// BluetoothManager.java
package com.example.smarthomebluetooth;

import java.util.UUID;

public class BluetoothManager {
    public static ConnectedThread1 connectedThread1 = null;

    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String SERVICE_NAME = "SmartHomeBT";
}
