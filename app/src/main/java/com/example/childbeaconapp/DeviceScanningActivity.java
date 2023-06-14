package com.example.childbeaconapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeviceScanningActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private Handler mHandler = new Handler();

    private int meterDistance = 0;

    ListView deviceListView;
    ArrayAdapter<String> deviceListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scanning);

        deviceListView = findViewById(R.id.etdevice_list_id);
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        deviceListView.setAdapter(deviceListAdapter);

        peripheralTextView = (TextView) findViewById(R.id.etPeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = (Button) findViewById(R.id.etStartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.etStopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = (String) parent.getItemAtPosition(position);
                showSaveDeviceDialog(selectedDevice);
            }
        });
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        private boolean mIsDeviceFound = false;
        private Set<String> mUniqueDeviceNames = new HashSet<>();

        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // Check if device name and address are not null
            String deviceName = result.getScanRecord().getDeviceName();
            String deviceAddress = result.getDevice().getAddress();
            if (deviceName != null && deviceAddress != null) {
                mIsDeviceFound = true;
                mUniqueDeviceNames.add(deviceName + "\nMAC: " + deviceAddress);

                // Update the deviceListAdapter with the new devices
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceListAdapter.clear();
                        deviceListAdapter.addAll(mUniqueDeviceNames);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // Handle scan failure
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                    Log.i("location", "location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        deviceListAdapter.clear();
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        Log.i("stop scan", "stopping scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });

        //  deviceListView.setAdapter(null);
    }

    private int getMeter(int rssi) {
        int i = rssi * -1;
        meterDistance = i/30;
        return meterDistance;
    }

    private void saveDeviceToDeviceList(String deviceInfo) {
        DeviceDbHelper dbHelper = new DeviceDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] parts = deviceInfo.split("\nMAC: ");
        String deviceName = parts[0];
        String deviceMacAddress = parts[1];

        ContentValues values = new ContentValues();
        values.put("name", deviceName);
        values.put("mac_address", deviceMacAddress);

        long newRowId = db.insert("devices", null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Device saved: " + deviceInfo, Toast.LENGTH_SHORT).show();
            // Navigate to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else {
            Toast.makeText(this, "Failed to save device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveDeviceDialog(String deviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Device");
        builder.setMessage("Do you want to save this device?\n\n" + deviceInfo);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveDeviceToDeviceList(deviceInfo);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }


}