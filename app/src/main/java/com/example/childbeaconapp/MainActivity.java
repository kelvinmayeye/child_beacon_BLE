package com.example.childbeaconapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private List<BluetoothDevice> nearbyDevices = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Check if Bluetooth is supported on the device
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check if Bluetooth is turned on
        if (!bluetoothAdapter.isEnabled()) {
            // Prompt the user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is already turned on, start scanning for nearby BLE devices
            scanForNearbyDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth has been turned on, start scanning for nearby BLE devices
                scanForNearbyDevices();
            } else {
                // User refused to turn on Bluetooth, show a message and exit the app
                Toast.makeText(this, "Bluetooth must be turned on to use this app", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void scanForNearbyDevices() {
        // TODO: Scan for nearby BLE devices and add them to the nearbyDevices list

        List<Map<String, String>> data = new ArrayList<>();

        // Iterate over the list of nearby BLE devices and add them to the data list
        for (BluetoothDevice device : nearbyDevices) {
            Map<String, String> item = new HashMap<>();
            item.put("name", device.getName());
            item.put("signal", device.getAddress() + " dBm");
            data.add(item);
        }

        // Update the count of nearby devices
        TextView countTextView = findViewById(R.id.count_text_view);
        countTextView.setText("Scanned devices: " + nearbyDevices.size());

        // Show or hide the empty view depending on whether any devices were found
        TextView emptyView = findViewById(R.id.empty_view);
        emptyView.setVisibility(nearbyDevices.isEmpty() ? View.VISIBLE : View.GONE);

        // Log the list of nearby devices
        Log.d("ScanNearbyDevices", "List of nearby devices: " + nearbyDevices.toString());

        Log.e("some error","This log shows sample error");

        String[] from = {"name", "signal"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, from, to);
        ListView listView = findViewById(R.id.device_list);
        listView.setAdapter(adapter);
    }


}