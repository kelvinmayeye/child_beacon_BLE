package com.example.childbeaconapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        List<Item> items =new ArrayList<Item>();
        items.add(new Item("kelvin Mayeye","kelvinmayeye@gmail.com",R.drawable.b));
        items.add(new Item("Remie Ongala","remiee2002@gmail.com",R.drawable.b));
        items.add(new Item("Juma Ally","jumaally123@gmail.com",R.drawable.b));
        items.add(new Item("Samson kalinga","sungura12daat.com",R.drawable.b));
        items.add(new Item("odama ogiri","ongirioddy120@gmail.com",R.drawable.b));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(),items));


    }


}
