This library provides an implementation of a service for BLE connectivity and data communication with a device.
You can connect to a BLE device and send and receive data from it.

# Usage

## Manifest

First to use this library you have to declare the service in your manifest like this:

```

<service
    android:name="com.andrearantin.blemanager.BLEService"
    android:enabled="true"
    android:stopWithTask="true" />

```
