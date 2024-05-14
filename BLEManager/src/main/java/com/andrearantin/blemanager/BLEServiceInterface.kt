package com.andrearantin.blemanager

import android.bluetooth.BluetoothGattCharacteristic
import com.andrearantin.blemanager.data.BLEConnectionEvent


internal interface BLEServiceInterface {

    fun onBLEConnectionStateChanged(bleConnectionEvent: BLEConnectionEvent)
    fun onBLEDataReceived(bluetoothGattCharacteristic: BluetoothGattCharacteristic)
}