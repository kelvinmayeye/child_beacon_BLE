package com.example.childbeaconapp;

public class Item {
    String devicename;
    String distance;
    int image;

    public Item(String devicename, String distance, int image) {
        this.devicename = devicename;
        this.distance = distance;
        this.image = image;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
