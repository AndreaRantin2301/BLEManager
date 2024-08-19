package com.andrearantin.blemanagerexample.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager

import androidx.lifecycle.ViewModel
import com.andrearantin.blemanager.BLEDataManager
import com.andrearantin.blemanager.BLEService

/**
 * Created by: Andrea Rantin
 * Date: 19/06/2024
 * Time: 09:28
 */

class TestViewsNoHiltViewModel : ViewModel(){

    private val bleService : BLEService;
    private val bleDataManager : BLEDataManager;

    init {
        val bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bleDataManager = BLEDataManager()
        bleService = BLEService(bluetoothAdapter,bleDataManager)
    }

}