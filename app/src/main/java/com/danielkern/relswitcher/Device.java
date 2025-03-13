package com.danielkern.relswitcher;

public class Device {
    boolean enabled;
    String name;
    String number;

    public Device() {
        this.enabled = false;
        this.name = "";
        this.number = "";
    }

    public Device(boolean enabled, String name, String number) {
        this.enabled = enabled;
        this.name = name;
        this.number = number;
    }
}
