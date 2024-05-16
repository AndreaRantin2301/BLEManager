package com.andrearantin.blemanagerexample;

public class NDKBridge {

    public static native byte crcFast(byte[] message, int nBytes);
}
