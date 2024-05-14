package com.andrearantin.blemanager

/**
 * Class to represent a remote BLE device
 * @param deviceName name of the BLE device
 * @param deviceAddress MAC address of the BLE device
 */
class BLEDevice(deviceName : String, deviceAddress : String) {

    private var deviceName : String = ""
    private var deviceAddress : String = ""

    init {
        this.deviceName = deviceName
        this.deviceAddress = deviceAddress
    }

    override fun equals(other: Any?): Boolean {

        if (other == null) return false

        val compareObj : BLEDevice = other as? BLEDevice ?: return false

        return ((this.deviceName == compareObj.deviceName) && (this.deviceAddress == compareObj.deviceAddress))
    }

    override fun hashCode(): Int {
        return (deviceName.hashCode() + deviceAddress.hashCode())
    }

}