# SmartHomeController

SmartHomeController is an Android application designed to simulate a smart home control system using **Bluetooth communication** between two smartphones. It uses a **client-server architecture** to allow one phone to act as a remote controller, and another as a server connected to a REST API.

---

## Project Overview

This application was developed as part of a mobile programming course project at INP ENSEEIHT. Its main goal is to demonstrate key Android development concepts such as:

- Activity lifecycle
- Bluetooth SPP communication
- Multi-threading (using `Handler` and custom threads)
- JSON message exchange
- REST API communication with [Volley](https://developer.android.com/training/volley)

---

## Architecture

### Client (Phone 1)
- Connects to the server via Bluetooth using a specific UUID.
- Displays a dynamic UI listing connected devices (received as JSON).
- Sends JSON control commands to the server (e.g., `{"deviceId": "101", "action": "turnOnOff"}`).

### Server (Phone 2)
- Accepts Bluetooth connections via `BluetoothServerSocket`.
- Periodically fetches device states from a REST API every 10 seconds.
- Sends device status to the client and relays control commands via HTTP POST requests.

---

## Communication

- **Bluetooth Socket**: Used for real-time communication between the client and server.
- **REST API**: Server fetches (`GET`) and updates (`POST`) smart device states using Volley.
- **ConnectedThread1**: A custom thread used to manage the Input/Output streams over Bluetooth.
- **Message Delimiters**: Used to handle truncated JSON over Bluetooth and ensure complete message parsing.

---

## Technologies

| Component        | Tech Used              |
|------------------|------------------------|
| Language         | Java                   |
| Platform         | Android                |
| Communication    | Bluetooth SPP + JSON   |
| API Integration  | Android Volley         |
| Threading        | Handler + Custom Thread|
| UI               | XML Layout + Views     |

---

## How to Use

1. **Install the app on two Android phones**.
2. Launch the app on both phones:
   - One in **Server mode** to connect to the REST API.
   - One in **Client mode** to control devices.
3. Pair devices via Bluetooth.
4. Start the connection from the client.
5. Control devices (e.g., lights, thermostats) using the on-screen buttons.

---
## Author

- Aicha ELIDRISSI  

---


