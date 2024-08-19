package com.andrearantin.blemanager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import com.andrearantin.blemanager.data.BLEScanCode
import com.andrearantin.blemanager.data.BLEScanResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Created by: Andrea Rantin
 * Date: 19/08/2024
 * Time: 10:43
 */
@SuppressLint("MissingPermission")
class BLEScanner @Inject constructor(
    private val bleService: BLEService,
    private val bluetoothAdapter: BluetoothAdapter?, //TODO VEDERE SE FARE COSI O PRENDERLO DA SERVICE
    private val bleScanner : BluetoothLeScanner?
) {

    companion object {
        const val DEFAULT_SCAN_DURATION : Long = 10000L
    }

    val scanDuration = DEFAULT_SCAN_DURATION
    //val scanResultChannel = Channel<BLEDevice?>()
    val bleScanChannel = Channel<BLEScanResult>()
    private val handler: Handler = Handler() //TODO VEDERE SE FARE MEGLIO

    private val scanCallback : ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
           val bleDevice : BLEDevice? = result?.device?.let { BLEDevice(it.name, it.address) }
            runBlocking {
               bleScanChannel.send(BLEScanResult(statusCode = BLEScanCode.BLE_SCAN_DEVICE_FOUND,bleDevice = bleDevice))
            }
        }
    }

    fun startBLEScan() {
        bleService.disconnect()
        if (!isBLEReady()){
            runBlocking {
                bleScanChannel.send(BLEScanResult(statusCode = BLEScanCode.BLE_NOT_READY, bleDevice = null))
            }
            return
        }
        try {
            handler.postDelayed(
                kotlinx.coroutines.Runnable {
                    stopBLEScan()
                    runBlocking {
                        bleScanChannel.send(BLEScanResult(statusCode = BLEScanCode.BLE_SCAN_STOPPED, bleDevice = null))
                    }
                },scanDuration)
            bleScanner?.startScan(scanCallback)

        }catch (e : Exception){
            runBlocking {
                bleScanChannel.send(BLEScanResult(statusCode = BLEScanCode.BLE_SCAN_FAILED, bleDevice = null))
            }
            return
        }

        runBlocking {
            bleScanChannel.send(BLEScanResult(statusCode = BLEScanCode.BLE_SCAN_STARTED, bleDevice = null))
        }
    }

    fun stopBLEScan(){
        bleScanner?.stopScan(scanCallback)
        handler.removeCallbacksAndMessages(null)
    }

    private fun isBLEReady() : Boolean{
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        return true
    }
}