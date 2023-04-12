package com.example.childbeaconapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private String mDeviceAddress;
    private TextView mDistanceTextView;
    private TextView mConnectedDevicesTextView;

    private static final double A_VALUE = 50.0;
    private static final double N_VALUE = 2.0;

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        TextView mConnectedDevicesTextView = findViewById(R.id.connected_devices_text_view);


        // Get the device address of the paired Bluetooth device.
        mDeviceAddress = "83:bb:59:55:fe:8d"; // Replace with your device address.

        mDistanceTextView = findViewById(R.id.distance_text_view);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onResume() {
        super.onResume();

        // Check if Bluetooth is enabled and the device is paired.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e("MainActivity", "Bluetooth is not enabled.");
            return;
        }

        // Get a list of all currently connected Bluetooth devices
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

// Display the list of connected devices in the TextView
        if (connectedDevices.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (BluetoothDevice device : connectedDevices) {
                sb.append("\n").append(device.getName()).append(" - ").append(device.getAddress());
            }
            mConnectedDevicesTextView.setText("Connected Devices:" + sb.toString());
        } else {
            mConnectedDevicesTextView.setText("No connected devices");
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

        // Check if the device is paired.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted. Request the permission.
            int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 0;
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
            return;
        }

        try {
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                Log.e("MainActivity", "Device is not paired.");
                return;
            }

            // Start a thread to continuously read the RSSI value of the device.
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.S)
                @Override
                public void run() {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted.
                        return;
                    }

                    // Permission is granted. Read the RSSI value.
                    device.fetchUuidsWithSdp();
                    int rssi = mBluetoothAdapter.getRemoteDevice(mDeviceAddress).getBondState();
                    double distance = calculateDistance(rssi);
                    mDistanceTextView.setText(String.format("Distance: %.2f meters", distance));
                    handler.postDelayed(this, 1000); // Read RSSI value every 1 second.
                }
            });
        } catch (SecurityException e) {
            // Permission is not granted.
            Log.e("MainActivity", "Bluetooth permission not granted.", e);
        }
    }

    private double calculateDistance(int rssi) {
        double distance;
        if (rssi == 0) {
            distance = -1.0;
        } else {
            double ratio = rssi * 1.0 / A_VALUE;
            if (ratio < 1.0) {
                distance = Math.pow(ratio, N_VALUE);
            } else {
                distance = (0.89976) * Math.pow(ratio, N_VALUE) + 0.111;
            }
        }
        return distance;
    }
}
