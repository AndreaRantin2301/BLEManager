package com.andrearantin.blemanager

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.andrearantin.blemanager.data.BLEConnectionEvent
import com.andrearantin.blemanager.utils.BLEManagerLogger
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

/**
 * Service for BLE connection and communication.
 * This has 2 channels for delivering connection results and characteristic data
 * @property serviceUuid primary service UUID of the BLE device
 * @property writeUuid write service UUID of the BLE device
 * @property readUuid read service UUID of the BLE device
 */
@OptIn(ExperimentalStdlibApi::class)
@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BLEService @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val bleDataManager: BLEDataManager
) : Service(){

    companion object {
        private val TAG = BLEService::class.qualifiedName
    }

    inner class LocalBinder : Binder() {
        val bleService : BLEService get() = this@BLEService
    }
    private val mBinder : IBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        BLEManagerLogger.d(TAG, "BOUND BLE SERVICE $this")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        BLEManagerLogger.d(TAG, "UNBOUND BLE SERVICE")
        return super.onUnbind(intent)
    }

    private var _serviceUuid : UUID? = null
    private var _writeUuid : UUID? = null
    private var _readUuid : UUID? = null
    private val bleServiceInterface : BLEServiceInterface = bleDataManager

    var serviceUuid : UUID?
        get() = _serviceUuid
        set(uuid){
            _serviceUuid = uuid
        }
    var writeUuid : UUID?
        get() = _writeUuid
        set(uuid){
            _writeUuid = uuid
        }
    var readUuid : UUID?
        get() = _readUuid
        set(uuid){
            _readUuid = uuid
        }

    private var bluetoothGatt : BluetoothGatt? = null
    private var bluetoothDeviceAddress : String = ""

    fun disconnect(){
        if (bluetoothAdapter == null || bluetoothGatt == null){
            BLEManagerLogger.e(TAG, "TRIED TO DISCONNECT BUT BLE ADAPTER OR GATT ARE NULL!")
            return
        }
        bluetoothGatt!!.disconnect()
    }

    fun connect(address : String?) : Boolean{
        BLEManagerLogger.d(TAG, "BLE SERVICE CONNECTION CALLED")
        if (bluetoothAdapter == null){
            BLEManagerLogger.e(TAG, "TRIED TO CONNECT BUT BLE ADAPTER IS NULL!")
            return false
        }

        if (address.isNullOrEmpty()){
            BLEManagerLogger.e(TAG, "DEVICE ADDRESS IS NULL OR EMPTY DURING CONNECTION!")
            return false
        }

        if (bluetoothDeviceAddress.isNotEmpty() && address == bluetoothDeviceAddress){
            BLEManagerLogger.d(TAG, "TRYING TO USE AN EXISTING BLUETOOTH GATT FOR CONNECTION")
            return bluetoothGatt!!.connect()
        }

        val device : BluetoothDevice? = bluetoothAdapter.getRemoteDevice(address)
        if (device == null){
            BLEManagerLogger.w(TAG, "BLE DEVICE NOT FOUND. UNABLE TO CONNECT")
            return false
        }

        bluetoothGatt = device.connectGatt(this,false,gattCallback)
        if (bluetoothGatt == null){
            BLEManagerLogger.e(TAG, "BLUETOOTH GATT NULL IN CONNECTION!")
            return false
        }
        BLEManagerLogger.d(TAG, "TRYING TO CREATE A NEW CONNECTION")
        bluetoothDeviceAddress = address
        return true
    }

    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            BLEManagerLogger.d(TAG, "NEW CONNECTION STATE: $newState STATUS: $status")
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    BLEManagerLogger.d(TAG, "CONNECTED TO GATT")
                    BLEManagerLogger.d(TAG, "ATTEMPTING SERVICE DISCOVERY")
                    val serviceDiscovery = bluetoothGatt?.discoverServices()
                    BLEManagerLogger.d(TAG, "SERVICE DISCOVERY SUCCESS: $serviceDiscovery")
                    bleServiceInterface.onBLEConnectionStateChanged(BLEConnectionEvent.BLE_CONNECTED)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    BLEManagerLogger.d(TAG, "DISCONNECTED FROM GATT")
                    bleServiceInterface.onBLEConnectionStateChanged(BLEConnectionEvent.BLE_DISCONNECTED)
                }
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            BLEManagerLogger.d(TAG, "SERVICES DISCOVERED WITH STATUS: $status")
            if (status == BluetoothGatt.GATT_SUCCESS){
                gatt?.services?.forEach { service ->
                    if (service.uuid.equals(_serviceUuid)){
                        BLEManagerLogger.d(TAG, "PRIMARY SERVICE FOUND")
                        service.characteristics.forEach { charateristic ->
                            if (charateristic.uuid.equals(_readUuid)){
                                BLEManagerLogger.d(TAG, "READ CHARATERISTIC FOUND")
                                val enableNotificationRes = enableCharateristicNotification(charateristic)
                                if (!enableNotificationRes){
                                    BLEManagerLogger.e(TAG, "COULD NOT ENABLE NOTIFICATION ON CHARATERISTIC!")
                                    return
                                }
                                BLEManagerLogger.d(TAG, "SERVICES DISCOVERY COMPLETED")
                                bleServiceInterface.onBLEConnectionStateChanged(BLEConnectionEvent.BLE_SERVICES_DISCOVERED)
                            }
                        }
                    }
                }
            }else{
                BLEManagerLogger.e(TAG, "SERVICE DISCOVERY ERROR WITH STATUS: $status")
            }
        }

        @Deprecated("OLD CALLBACK USED FOR <33 APIS")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            BLEManagerLogger.d(TAG, "CHARACTERISTIC READ WITH STATUS: $status")
            if (status == BluetoothGatt.GATT_SUCCESS){
                if (characteristic == null) return
                bleServiceInterface.onBLEDataReceived(characteristic.value)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            BLEManagerLogger.d(TAG, "CHARACTERISTIC READ WITH STATUS: $status")
            if (status == BluetoothGatt.GATT_SUCCESS){
                bleServiceInterface.onBLEDataReceived(value)
            }
        }

        @Deprecated("OLD CALLBACK USED FOR <33 APIS")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            BLEManagerLogger.d(TAG, "CHARACTERISTIC CHANGED API <33")
            if (characteristic == null) return
            BLEManagerLogger.d(TAG, "VALUE ${characteristic.value.toTypedArray()}")
            BLEManagerLogger.d(TAG, "HEX STRING ${characteristic.value.contentToString()}")
            bleServiceInterface.onBLEDataReceived(characteristic.value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            BLEManagerLogger.d(TAG, "CHARACTERISTIC CHANGED API >=33")
            BLEManagerLogger.d(TAG, "VALUE ${value.toTypedArray()}")
            BLEManagerLogger.d(TAG, "HEX STRING ${value.contentToString()}")
            bleServiceInterface.onBLEDataReceived(value)
        }
    }

    private fun enableCharateristicNotification(gattCharacteristic: BluetoothGattCharacteristic) : Boolean {
        if (bluetoothAdapter == null || bluetoothGatt == null){
            BLEManagerLogger.e(TAG, "ADAPTER OR GATT NOT ENABLED DURING NOTIFICATION ENABLING!")
            return false
        }
        //TODO FIXARE DEPRECATED
        bluetoothGatt!!.setCharacteristicNotification(gattCharacteristic,true)
        gattCharacteristic.descriptors.forEach {descriptor ->
            BLEManagerLogger.d(TAG, "GATT DESCRIPTOR: ${descriptor.uuid}")
            val enableNotificationRes = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            BLEManagerLogger.d(TAG, "DESCRIPTOR ENABLE NOTIFICATION RESULT: $enableNotificationRes")
            val writeDescriptorRes = bluetoothGatt!!.writeDescriptor(descriptor)
            BLEManagerLogger.d(TAG,"WRITE DESCRIPTOR RESULT: $writeDescriptorRes")
            //TODO VEDERE SE FARE RETURN FALSE IN FALLIMENTO
        }
        return true
    }

    fun writeBytes(data : ByteArray?) : Boolean {
        val characteristic : BluetoothGattCharacteristic = bluetoothGatt?.getService(_serviceUuid)?.getCharacteristic(_writeUuid) ?: return false
        if (data == null) return false
        //TODO FIXARE DEPRECATED
        characteristic.setValue(data)
        val result : Boolean = bluetoothGatt?.writeCharacteristic(characteristic) == true
        BLEManagerLogger.d(TAG, "WRITE CHARACTERISTIC RESULT: $result")
        BLEManagerLogger.d(TAG, "VALUE: ${data.contentToString()}")
        return true
    }
}